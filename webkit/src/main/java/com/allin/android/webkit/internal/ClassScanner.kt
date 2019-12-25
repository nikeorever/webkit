package com.allin.android.webkit.internal

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RestrictTo
import dalvik.system.DexFile
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

private const val VM_WITH_MULTIDEX_VERSION_MAJOR = 2
private const val VM_WITH_MULTIDEX_VERSION_MINOR = 1
private const val PREFS_FILE = "multidex.version"
private const val KEY_DEX_NUMBER = "dex.number"
private const val CODE_CACHE_NAME = "code_cache"
private const val CODE_CACHE_SECONDARY_FOLDER_NAME = "secondary-dexes"
private const val EXTRACTED_SUFFIX = ".zip"
private const val EXTRACTED_NAME_EXT = ".classes"

private val diskIO: ExecutorService = Executors.newFixedThreadPool(4, object : ThreadFactory {
    private val mThreadId: AtomicInteger = AtomicInteger(0)

    override fun newThread(r: Runnable): Thread {
        val t = Thread(r)
        t.name = "Dex_load_io_${mThreadId.getAndIncrement()}"
        return t
    }
})

private fun executeOnDiskIO(runnable: () -> Unit) {
    diskIO.execute(runnable)
}

@RestrictTo(value = [RestrictTo.Scope.LIBRARY_GROUP_PREFIX])
fun listClassesInPackageOf(context: Context, packageName: String): Set<String>? {
    if (packageName.isEmpty()) {
        return null
    }
    val allDexSourceDirs = allDexSourceDirs(context)
    if (allDexSourceDirs.isNotEmpty()) {
        val latch = CountDownLatch(allDexSourceDirs.size)
        val clsPath = HashSet<String>()
        allDexSourceDirs.forEach { sourceDir ->
            executeOnDiskIO {
                var dexFile: DexFile? = null
                try {
                    dexFile = if (sourceDir.endsWith(EXTRACTED_SUFFIX)) {
                        DexFile.loadDex(sourceDir, "$sourceDir.tmp", 0)
                    } else {
                        DexFile(sourceDir)
                    }
                    val dexEntries: Enumeration<String> = dexFile!!.entries()
                    while (dexEntries.hasMoreElements()) {
                        val className = dexEntries.nextElement()
                        if (className.startsWith(packageName)) {
                            clsPath.add(className)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DexLoader", e.message.toString())
                } finally {
                    dexFile?.runCatching {
                        close()
                    }
                    latch.countDown()
                }
            }
        }
        latch.await()
        return clsPath
    } else {
        return null
    }
}

private fun allDexSourceDirs(context: Context): List<String> {
    return ArrayList<String>().also { allDexSourceDirs ->
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
        allDexSourceDirs.add(appInfo.sourceDir)

        if (!isVMMultiDexCapable()) {
            val totalDexNumber = getMultiDexPreferences(context)!!.getInt(KEY_DEX_NUMBER, 1)
            val dexDir = File(
                appInfo.dataDir,
                CODE_CACHE_NAME + File.separator + CODE_CACHE_SECONDARY_FOLDER_NAME
            )
            val sourceDir = File(appInfo.sourceDir)
            for (secondaryNumber in 2..totalDexNumber) {
                val fileName: String =
                    sourceDir.name + EXTRACTED_NAME_EXT + secondaryNumber + EXTRACTED_SUFFIX
                val extractedFile = File(dexDir, fileName)
                if (extractedFile.isFile) {
                    allDexSourceDirs.add(extractedFile.absolutePath)
                } else {
                    throw IOException("Missing extracted secondary dex file '" + extractedFile.path + "'")
                }
            }
        }
    }
}

/**
 * Identifies if the current VM has a native support for multidex, meaning there is no need for
 * additional installation by this library.
 * @return true if the VM handles multidex
 */
private fun isVMMultiDexCapable(versionString: String? = System.getProperty("java.vm.version"))
        : Boolean {
    var isMultiDexCapable = false
    if (versionString != null) {
        val tokenizer = StringTokenizer(versionString, ".")
        val majorToken = if (tokenizer.hasMoreTokens()) tokenizer.nextToken() else null
        val minorToken = if (tokenizer.hasMoreTokens()) tokenizer.nextToken() else null
        if (majorToken != null && minorToken != null) {
            try {
                val major = majorToken.toInt()
                val minor = minorToken.toInt()
                isMultiDexCapable =
                    (major > VM_WITH_MULTIDEX_VERSION_MAJOR
                            || (major == VM_WITH_MULTIDEX_VERSION_MAJOR
                            && minor >= VM_WITH_MULTIDEX_VERSION_MINOR))
            } catch (e: NumberFormatException) { // let isMultidexCapable be false
            }
        }
    }
    Log.i(
        "MultiDexChecker", "VM with version " + versionString +
                if (isMultiDexCapable) " has multidex support" else " does not have multidex support"
    )
    return isMultiDexCapable
}


/**
 * Get the MuliDex [SharedPreferences] for the current application. Should be called only
 * while owning the lock on [.LOCK_FILENAME].
 */
@SuppressLint("WrongConstant", "ObsoleteSdkInt")
private fun getMultiDexPreferences(context: Context): SharedPreferences? {
    return context.getSharedPreferences(
        PREFS_FILE,
        if (Build.VERSION.SDK_INT < 11 /* Build.VERSION_CODES.HONEYCOMB */) Context.MODE_PRIVATE
        else Context.MODE_PRIVATE or 0x0004 /* Context.MODE_MULTI_PROCESS */
    )
}