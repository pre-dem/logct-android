package qiniu.dem.logct;

class Logan {

    private static final String LIBRARY_NAME = "logan";

    private static boolean loaded;

    static {
        try {
            System.loadLibrary(LIBRARY_NAME);
            loaded = true;
        } catch (Throwable e) {
            e.printStackTrace();
            loaded = false;
        }
    }

    private boolean initialized;
    private boolean opened;

    static boolean isLoaded() {
        return loaded;
    }

    /**
     * 初始化Clogan
     *
     * @param dir_path 目录路径
     * @param max_file 最大文件值
     */
    private native int cinit(String cache_path, String dir_path, int max_file,
                             String encrypt_key_16, String encrypt_iv_16);

    private native int copen(String file_name);

    private native void cdebug(boolean is_debug);

    /**
     * @param flag        日志类型
     * @param log         日志内容
     * @param local_time  本地时间
     * @param thread_name 线程名称
     * @param thread_id   线程ID
     * @param is_main     是否主线程
     */
    private native int cwrite(int flag, String log, long local_time, String thread_name,
                              long thread_id, int is_main);

    private native void cflush();

    public int init(String cache_path, String dir_path, int max_file, String encrypt_key_16,
                    String encrypt_iv_16) {
        if (initialized) {
            return ErrorCode.OK;
        }
        if (!loaded) {
            return ErrorCode.CLOGAN_LOAD_SO_FAIL;
        }

        try {
            int code = cinit(cache_path, dir_path, max_file, encrypt_key_16, encrypt_iv_16);
            initialized = true;
            return code;
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            return ErrorCode.CLOGAN_INIT_FAIL_JNI;
        }
    }

    public void debug(boolean debug) {
        if (!initialized || !loaded) {
            return;
        }
        try {
            cdebug(debug);
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    public int open(String file_name) {
        if (!initialized || !loaded) {
            return ErrorCode.CLOGAN_LOAD_SO_FAIL;
        }
        try {
            int code = copen(file_name);
            opened = true;
            return code;
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            return ErrorCode.CLOGAN_INIT_FAIL_JNI;
        }
    }

    public void flush() {
        if (!opened || !loaded) {
            return;
        }
        try {
            cflush();
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }

    }

    public int write(int flag, String log, long local_time, String thread_name,
                     long thread_id, boolean is_main) {
        if (!opened || !loaded) {
            return ErrorCode.CLOGAN_LOAD_SO_FAIL;
        }
        try {
            int isMain = is_main ? 1 : 0;
            int code = cwrite(flag, log, local_time, thread_name, thread_id,
                    isMain);
            if (code == ErrorCode.CLOGAN_WRITE_SUCCESS) {
                code = ErrorCode.OK;
            }
            return code;
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            return ErrorCode.CLOGAN_INIT_FAIL_JNI;
        }
    }
}