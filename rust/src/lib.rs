#![cfg(target_os = "android")]
#![allow(non_snake_case)]

use jni::JNIEnv;
use jni::objects::{JObject, JString};
use jni::sys::jstring;
use std::ffi::{CStr, CString};

#[unsafe(no_mangle)]
pub unsafe extern "C" fn Java_org_kotfind_android_1course_MainActivity_hello(
    mut env: JNIEnv,
    _: JObject,
    j_recipient: JString,
) -> jstring {
    let recipient =
        CString::from(unsafe { CStr::from_ptr(env.get_string(&j_recipient).unwrap().as_ptr()) });

    let output = env
        .new_string("Hello ".to_owned() + recipient.to_str().unwrap())
        .unwrap();

    output.into_raw()
}
