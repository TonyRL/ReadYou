package me.ash.reader.infrastructure.di

import android.content.Context
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Provides singleton [ImageLoader] for Coil.
 */
@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
    ): ImageLoader {
        return ImageLoader.Builder(context)
            // This slightly improves scrolling performance
            .fetcherCoroutineContext(Dispatchers.Default)
            .decoderCoroutineContext(Dispatchers.Default)
            .components {
                // Shared OKHttpClient instance.
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
                // Support SVG decoding
                add(SvgDecoder.Factory())
                // Support GIF decoding
                add(
                    if (SDK_INT >= Build.VERSION_CODES.P) {
                        AnimatedImageDecoder.Factory()
                    } else {
                        GifDecoder.Factory()
                    }
                )
            }
            // Enable disk cache
            .diskCache(
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("images"))
                    .maxSizePercent(0.02)
                    .build()
            )
            // Enable memory cache
            .memoryCache(
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            )
            .build()
    }
}
