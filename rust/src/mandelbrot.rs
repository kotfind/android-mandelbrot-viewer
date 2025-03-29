use crate::log;
use itertools::iproduct;
use jni::{
    JNIEnv,
    objects::{JIntArray, JObject},
};

type Complex = num::complex::Complex<f64>;
type Color = i32;

pub struct RustMandelbrotGenerator {
    center_x: f64,
    center_y: f64,
    range: f64,
    bitmap_size: usize,
    max_iter: usize,
}

impl RustMandelbrotGenerator {
    pub fn from_jobject<'local>(env: &mut JNIEnv<'local>, this: &JObject<'local>) -> Self {
        let center_x = env.get_field(this, "centerX", "D").unwrap().d().unwrap();
        let center_y = env.get_field(this, "centerY", "D").unwrap().d().unwrap();
        let range = env.get_field(this, "range", "D").unwrap().d().unwrap();
        let bitmap_size = env.get_field(this, "bitmapSize", "I").unwrap().i().unwrap() as usize;
        let max_iter = env.get_field(this, "maxIter", "I").unwrap().i().unwrap() as usize;

        log(env, "Created RustMandelbrotGenerator instance");

        Self {
            center_x,
            center_y,
            range,
            bitmap_size,
            max_iter,
        }
    }

    pub fn get_pixels_as_jint_array<'local>(&self, env: &mut JNIEnv<'local>) -> JIntArray<'local> {
        log(env, "Generating pixels...");

        let pixels_rust = self.gen_pixels();
        let pixels_java = env.new_int_array(pixels_rust.len() as i32).unwrap();
        env.set_int_array_region(&pixels_java, 0, &pixels_rust)
            .unwrap();

        log(env, "Done generating pixels!");

        pixels_java
    }

    pub fn gen_pixels(&self) -> Vec<Color> {
        iproduct!(0..self.bitmap_size, 0..self.bitmap_size)
            .map(|(y, x)| {
                let c = self.x_y_to_complex((x as f64, y as f64));
                let iters = self.get_point_iters(c);
                let color = self.iters_to_color(iters);
                color
            })
            .collect()
    }

    fn x_y_to_complex(&self, (x, y): (f64, f64)) -> Complex {
        let sz = self.bitmap_size as f64;
        let rng = self.range as f64;
        let cx = self.center_x;
        let cy = self.center_y;

        let math_x = x as f64 / sz * rng - rng / 2.0 + cx;
        let math_y = y as f64 / sz * rng - rng / 2.0 + cy;

        Complex::new(math_x, math_y)
    }

    fn iters_to_color(&self, iters: usize) -> Color {
        let hue = 360.0 * iters as f64 / self.max_iter as f64;
        let (r, b, g) = hsv::hsv_to_rgb(360.0 - hue, 1.0, 1.0);
        let a = 0xffu32;

        // https://developer.android.com/reference/android/graphics/Color#encoding
        let color = (a << 24) | ((r as u32) << 16) | ((g as u32) << 8) | (b as u32);

        color as Color
    }

    fn get_point_iters(&self, c: Complex) -> usize {
        let mut z = Complex::new(0.0, 0.0);
        for iter in 0..self.max_iter {
            if z.norm() > 2.0 {
                return iter;
            }

            z = z * z + c
        }
        self.max_iter
    }
}
