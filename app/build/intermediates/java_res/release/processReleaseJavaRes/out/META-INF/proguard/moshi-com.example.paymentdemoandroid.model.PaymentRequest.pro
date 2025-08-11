-if class com.example.paymentdemoandroid.model.PaymentRequest
-keepnames class com.example.paymentdemoandroid.model.PaymentRequest
-if class com.example.paymentdemoandroid.model.PaymentRequest
-keep class com.example.paymentdemoandroid.model.PaymentRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.paymentdemoandroid.model.PaymentRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.example.paymentdemoandroid.model.PaymentRequest
-keepclassmembers class com.example.paymentdemoandroid.model.PaymentRequest {
    public synthetic <init>(com.example.paymentdemoandroid.model.Initiation,boolean,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
