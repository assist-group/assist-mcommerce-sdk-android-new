package ru.assist.sdk.dagger2

import com.google.gson.GsonBuilder
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.simpleframework.xml.convert.AnnotationStrategy
import org.simpleframework.xml.core.Persister
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.lang.reflect.Type

internal class XmlOrJsonConverterFactory : Converter.Factory() {
    private val xmlConverterFactory =
        SimpleXmlConverterFactory.createNonStrict(
            Persister(AnnotationStrategy())
        )
    private val jsonConverterFactory =
        GsonConverterFactory.create(
            GsonBuilder().setLenient().create()
        )

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        for (annotation in methodAnnotations) {
            if (annotation.annotationClass == Xml::class) {
                return xmlConverterFactory
                    .requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
            }
            if (annotation.annotationClass == Json::class) {
                return jsonConverterFactory
                    .requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
            }
        }
        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        for (annotation in annotations) {
            if (annotation.annotationClass == Xml::class || annotation.annotationClass == UrlXml::class) {
                return xmlConverterFactory.responseBodyConverter(type, annotations, retrofit)
            }
            if (annotation.annotationClass == Json::class) {
                return jsonConverterFactory.responseBodyConverter(type, annotations, retrofit)
            }
        }
        return super.responseBodyConverter(type, annotations, retrofit)
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class Xml

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class UrlXml

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class Json 