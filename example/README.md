# Running examples

## Build examples

### Provide Ethereum JSON RPC endpoint

You can use [Infura](https://infura.io/):
- register on the website
- get your project id
- add `ethereum.json.rpc.endpoint=https://mainnet.infura.io/v3/<your-project-id>`
  into `ethereum.properties` file

### Run build

Execute:
```
mvn install
```

## Ethereum identity preparation

1. Create Ethereum wallet in Ropsten network, get ETH and AGI, following
instructions on https://dev.singularitynet.io/docs/setup/create-a-wallet/

2. Install `snet-cli` tool (https://github.com/singnet/snet-cli)

```sh
$ sudo pip3 install snet-cli
```

3. Export private key from Metamask account and add it to `snet-cli`

```sh
$ snet identity create test-user key \
		--network ropsten \
        --private-key "<account-private-key>"
$ snet identity test-user
```

4. Deposit AGI to MultiPartyEscrow contract

```sh
$ snet account deposit 10 -y --gas-price 380000000000
```

5. Check account balance

```sh
$ snet account balance
```

Result should look like:
```
    account: <your account address>
    ETH: 0.96804863
    AGI: 0
    MPE: 10
```

## Run application

1. Create payment channel with SingularityNet

```sh
$ snet channel open-init snet default_group 0.1 +7days --gas-price 380000000000
```

`snet-cli` should return you the payment channel id.

2. Run application

```sh
$ mvn exec:java -Dexec.mainClass="io.singularitynet.sdk.example.CntkImageRecognition" -Dexec.args="<account-private-key> <channel-id>"
```

# Create your own service client

1. Create new maven project, see https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html

2. Use `snet-sdk-maven-plugin` to get protobuf API:

```
  <build>
    <plugins>
    ...
      <plugin>
        <groupId>io.singularitynet</groupId>
        <artifactId>snet-sdk-maven-plugin</artifactId>
        <version>0.2.0-SNAPSHOT</version>

        <executions>
          <execution>

            <configuration>
              <orgId>snet</orgId> <!-- organization id -->
              <serviceId>example-service</serviceId> <!-- service id -->
              <outputDir>${project.build.directory}/proto</outputDir> <!-- output dir -->
              <javaPackage>io.singularitynet.service.exampleservice</javaPackage> <!-- java package for classes generated -->
              <ethereumJsonRpcEndpoint>https://mainnet.infura.io/v3/infura-project-id</ethereumJsonRpcEndpoint>
            </configuration>

            <goals>
              <goal>get</goal>
            </goals>

          </execution>
        </executions>

      </plugin>
    ...
    </plugins>
  </build>
```

3. Use `protobuf-maven-plugin` to generate Java stubs of service API:
- [grpc-java README.md](https://github.com/grpc/grpc-java/blob/master/README.md)
- [protobuf-maven-plugin documentation](https://www.xolstice.org/protobuf-maven-plugin/)

4. Write Java client app using sdk, see [CntkImageRecognition.java](./cli/cntk-image-recognition/src/main/java/io/singularitynet/sdk/example/CntkImageRecognition.java) as example

