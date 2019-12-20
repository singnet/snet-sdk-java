# How to use SDK in Android app

1. To the project `build.gradle` script add:

- into `buildscript/repositories` section:
```
        mavenCentral()
        maven {
            url 'https://jitpack.io'
        }
        maven {
            url 'file:///mnt/fileserver/shared/vital/m2/repository'
        }
```

- into `buildscript/dependencies` section:
```
        classpath 'io.singularitynet:snet-sdk-gradle-plugin:1.0-SNAPSHOT'
        classpath 'com.google.protobuf:protobuf-gradle-plugin:0.8.10'
```

2. To the app `build.gradle` script add:
- at the beginning of the script:
```
apply plugin: 'io.singularitynet.sdk'
apply plugin: 'com.google.protobuf'
```

- into android section:
```
    compileOptions {
        sourceCompatibility = 1.8
        targetCompatibility = 1.8
    }
```

- before `dependencies` section:
```
repositories {
    maven {
        url 'https://jitpack.io'
    }
    maven {
        url 'file:///mnt/fileserver/shared/vital/m2/repository'
    }
}
```

- into `dependencies` section:
```
implementation 'io.singularitynet:snet-sdk-java:1.0-SNAPSHOT'
```

- at the end of the script:
```
import io.singularitynet.sdk.gradle.GetSingularityNetServiceApi

tasks.register('getExampleServiceApi', GetSingularityNetServiceApi) {
    orgId = 'snet'
    serviceId = 'example-service'
    outputDir = file("$buildDir/proto")
    javaPackage = 'io.singularitynet.service.exampleservice'

    ipfsRpcEndpoint = new java.net.URL('http://ipfs.singularitynet.io:80')
    ethereumJsonRpcEndpoint = new java.net.URL('https://mainnet.infura.io')
    getterEthereumAddress = '0xdcE9c76cCB881AF94F7FB4FaC94E4ACC584fa9a5'
    registryAddress = ''
}
tasks.preBuild.dependsOn('getExampleServiceApi')

def grpcVersion = '1.20.0'
def protobufVersion = '3.5.1'
def protocVersion = protobufVersion

android {
    sourceSets {
        main {
            proto {
                srcDir "$buildDir/proto"
            }
        }
    }
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:${protocVersion}" }
    plugins {
        grpc { artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}" }
        // TODO: use javalite as recommended for Android devices
        // javalite { artifact = "com.google.protobuf:protoc-gen-javalite:3.0.0" }
    }
    generateProtoTasks {
        all()*.builtins { remove java }
        all()*.plugins {
            grpc {}
            java {}
        }
    }
}
````
