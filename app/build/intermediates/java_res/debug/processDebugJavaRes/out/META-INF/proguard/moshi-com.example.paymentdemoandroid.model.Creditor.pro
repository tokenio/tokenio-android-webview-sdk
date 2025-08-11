-if class com.example.paymentdemoandroid.model.Creditor
-keepnames class com.example.paymentdemoandroid.model.Creditor
-if class com.example.paymentdemoandroid.model.Creditor
-keep class com.example.paymentdemoandroid.model.CreditorJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.paymentdemoandroid.model.Creditor
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.example.paymentdemoandroid.model.Creditor
-keepclassmembers class com.example.paymentdemoandroid.model.Creditor {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
