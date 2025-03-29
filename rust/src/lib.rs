#![cfg(target_os = "android")]
#![allow(non_snake_case)]

use jni::{
    JNIEnv,
    objects::{JIntArray, JObject, JValueGen},
};
use mandelbrot::RustMandelbrotGenerator;

mod mandelbrot;

#[unsafe(no_mangle)]
pub unsafe extern "C" fn Java_org_kotfind_android_1course_RustMandelbrotGenerator_genPixels<
    'local,
>(
    mut env: JNIEnv<'local>,
    this: JObject<'local>,
) -> JIntArray<'local> {
    let mandelbrotGenerator = RustMandelbrotGenerator::from_jobject(&mut env, &this);
    mandelbrotGenerator.get_pixels_as_jint_array(&mut env)
}

fn log<'local>(env: &mut JNIEnv<'local>, msg: impl ToString) {
    let logClass = env.find_class("android/util/Log").unwrap();

    env.call_static_method(logClass, "i", "(Ljava/lang/String;Ljava/lang/String;)I", &[
        JValueGen::Object(&env.new_string("RUST_LOG").unwrap()),
        JValueGen::Object(&env.new_string(msg.to_string()).unwrap()),
    ])
    .unwrap();
}
