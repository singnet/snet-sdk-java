# Using SDK in Android application

Sdk supports Android platfrom version 7.0 or higher (API level 24 or higher).

## Root project `gradle.properties`

It is convenient to add dependencies version into `gradle.properties` file:
```
snetSdkJavaVersion=0.3.2
grpcVersion=1.20.0
protobufVersion=3.5.1
```

## Root project `build.gradle`

Add Maven Central and Jitpack.io repositories into `buildscript/repositories`
section. Maven Central is used to get Gradle Protobuf plugin. Jitpack.io repo
contains SingularityNet SDK dependencies.
```
buildscript {
    repositories {
        ...
        mavenCentral()
        maven {
            url 'https://jitpack.io'
        }
        ...
    }
}
```

Add SingularityNet and Protobuf plugins into the classpath using
`buildscript/dependencies` section:
```
buildscript {
    ...
    dependencies {
        classpath 'com.github.singnet.snet-sdk-java:snet-sdk-gradle-plugin:${snetSdkJavaVersion}'
        classpath 'com.google.protobuf:protobuf-gradle-plugin:0.8.10'
    }
}
```

## Android app `build.gradle` script

Apply SingularityNet SDK and Protobuf plugins.
```
apply plugin: 'io.singularitynet.sdk'
apply plugin: 'com.google.protobuf'
```

Add Jitpack.io repository before `dependencies` section to get SingularityNet
Java SDK dependencies.
```
repositories {
    maven {
        url 'https://jitpack.io'
    }
}
```

Add SingularityNet Java SDK into `dependencies` section:
```
implementation 'io.grpc:grpc-okhttp:${grpcVersion}'
implementation 'org.slf4j:slf4j-android:1.7.30'
implementation 'com.github.singnet.snet-sdk-java:snet-sdk-java:${snetSdkJavaVersion}'
```

Add one Gradle task to download and unpack each SingularityNet service API you
are going to use. You should provide at least `orgId`, `serviceId` to specify
service. `javaPackage` to set convenient package for Java files to be
generated. `outputDir` to write resulting protobuf files.
```
tasks.register('getExampleServiceApi', io.singularitynet.sdk.gradle.GetSingularityNetServiceApi) {
    orgId = 'snet'
    serviceId = 'example-service'
    javaPackage = 'io.singularitynet.service.exampleservice'
    outputDir = file("$buildDir/proto")
    ethereumJsonRpcEndpoint = '<your-ethereum-endpoint>'
}
```

Add dir which was used to unpack service API protobuf on the previous step into
set of source dirs. Turn on support of Java 8 features.
```
android {
    sourceSets {
        main {
            proto {
                srcDir "$buildDir/proto"
            }
        }
    }
    compileOptions {
        sourceCompatibility = 1.8
        targetCompatibility = 1.8
    }
}
```

Use Protobuf compiler with gRPC plugin to compile SingularityNet service API
into Java code. gRPC and Protobuf versions below are recommended as they were
used to compile SingularityNet Java SDK.
```
protobuf {
    protoc { artifact = "com.google.protobuf:protoc:${protobufVersion}" }
    plugins {
        grpc { artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}" }
    }
    generateProtoTasks {
        all()*.builtins { remove java }
        all()*.plugins {
            grpc {}
            java {}
        }
    }
}
```
