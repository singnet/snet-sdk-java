# SingularityNet Java SDK examples

This directory contains demonstration examples of SingularityNet Java SDK
usage. Examples call services which are running on Ethereum mainnet. In order
to run these examples some amount of Ethereum and AGI tokens is required.

If you are trying to connect to a service in a test Ethereum network you can
create an identity and get ETH and AGI, following instructions on
https://dev.singularitynet.io/docs/setup/create-a-wallet/. In such case you
should replace `mainnet` by test network name (`ropsten` for instance) in the
steps below.

To prepare for launching application:

1. Run SingularityNet platform docker and setup `snet-cli` tool:
```sh
$ docker run -ti singularitynet/snet-platform:latest
# snet network mainnet
# snet set default_eth_rpc_endpoint https://mainnet.infura.io/v3/e7732e1f679e461b9bb4da5653ac3fc2
```

2. Export private key from Metamask account and add it to `snet-cli` tool
   configuration. Set `--network` and `--private-key` parameters according to
   the network you are going to use:
```sh
# snet identity create test-user key \
		--network mainnet \
        --private-key "<account-private-key>"
# snet identity test-user
```

3. Deposit AGIs into MultiPartyEscrow contract and check account balance:

```sh
# snet account deposit 10 -y
# snet account balance
```

Result should look like:
```
    account: <your account address>
    ETH: 0.96804863
    AGI: 0
    MPE: 10
```

4. Exit docker and build examples:
```sh
$ mvn install
```

