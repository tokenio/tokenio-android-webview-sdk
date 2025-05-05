-if class com.example.paymentdemoandroid.model.Initiation
-keepnames class com.example.paymentdemoandroid.model.Initiation
-if class com.example.paymentdemoandroid.model.Initiation
-keep class com.example.paymentdemoandroid.model.InitiationJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.paymentdemoandroid.model.Initiation
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.example.paymentdemoandroid.model.Initiation
-keepclassmembers class com.example.paymentdemoandroid.model.Initiation {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.example.paymentdemoandroid.model.Amount,java.lang.String,com.example.paymentdemoandroid.model.Creditor,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
