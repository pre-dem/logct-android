/*
 * Copyright (c) 2018-present, 美团点评
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

#ifndef ANDROID_CLOGAN_JNI_H
#define ANDROID_CLOGAN_JNI_H
#ifdef __cplusplus
extern "C"
{
#endif

#include <jni.h>
#include <clogan_core.h>

JNIEXPORT jint JNICALL
Java_qiniu_dem_logct_Logan_cwrite(JNIEnv *env, jobject thiz,
                                  jint flag, jstring log,
                                  jlong local_time, jstring thread_name,
                                  jlong thread_id, jint is_main);

JNIEXPORT jint JNICALL
Java_qiniu_dem_logct_Logan_cinit(JNIEnv *env, jobject thiz,
                                 jstring cache_path, jstring dir_path, jint max_file,
                                 jstring encrypt_key_16, jstring encrypt_iv_16);

JNIEXPORT jint JNICALL
Java_qiniu_dem_logct_Logan_copen(JNIEnv *env, jobject thiz, jstring file_name);

JNIEXPORT void JNICALL
Java_qiniu_dem_logct_Logan_cflush(JNIEnv *env, jobject thiz);

JNIEXPORT void JNICALL
Java_qiniu_dem_logct_Logan_cdebug(JNIEnv *env, jobject thiz, jboolean is_debug);


#ifdef __cplusplus
}
#endif

#endif //ANDROID_CLOGAN_JNI_H
