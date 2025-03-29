#![cfg(target_os = "android")]
#![allow(non_snake_case)]

use jni::{
    JNIEnv,
    objects::{JIntArray, JObject, JValueGen},
};

#[unsafe(no_mangle)]
pub unsafe extern "C" fn Java_org_kotfind_android_1course_RustMandelbrotGenerator_genPixels<
    'local,
>(
    mut env: JNIEnv<'local>,
    this: JObject<'local>,
) -> JIntArray<'local> {
    let bitmap_size = env.get_field(this, "bitmapSize", "I").unwrap().i().unwrap() as usize;

    let mut pixels = vec![0; bitmap_size * bitmap_size];
    for pixel in &mut pixels {
        *pixel = 0xFF00FFFFu32 as i32;
    }

    let pixelsJava = env
        .new_int_array((bitmap_size * bitmap_size) as i32)
        .unwrap();
    env.set_int_array_region(&pixelsJava, 0, &pixels).unwrap();
    return pixelsJava;
}

fn log<'local>(env: &mut JNIEnv<'local>, msg: impl ToString) {
    let logClass = env.find_class("android/util/Log").unwrap();

    env.call_static_method(logClass, "e", "(Ljava/lang/String;Ljava/lang/String;)I", &[
        JValueGen::Object(&env.new_string("RUST_LOG").unwrap()),
        JValueGen::Object(&env.new_string(msg.to_string()).unwrap()),
    ])
    .unwrap();
}
