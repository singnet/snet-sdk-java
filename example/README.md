# Running example

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
$ mvn exec:java -Dexec.args="<account-private-key> <channel-id>"
```

# Create your own service client

1. Create new maven project, see https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html

2. Get service API

```sh
$ mkdir -p src/main/proto
$ snet service get-api-registry snet cntk-image-recon src/main/proto
```

3. Add Java package option

```sh
$ echo 'option java_package = "recognition";' >> src/main/proto/image_recon.proto
```

4. Write Java client app using sdk, see CntkImageRecognition.java as example

