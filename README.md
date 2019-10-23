# SingularityNet Java SDK

[![CircleCI](https://circleci.com/gh/vsbogd/snet-sdk-java.svg?style=svg)](https://circleci.com/gh/vsbogd/snet-sdk-java)
[![codecov](https://codecov.io/gh/vsbogd/snet-sdk-java/branch/master/graph/badge.svg)](https://codecov.io/gh/vsbogd/snet-sdk-java)

## Class diagram

![Class diagram](https://www.plantuml.com/plantuml/svg/ZLLTJzim57tth_1ZDXN-05M5sWsK8LZLT4_J7enp2PPSExDlqaN0VyTs7PPFuCsvFZxtd7Fjzj8YCrWT924eK0BDX2egoOp0vWwG_c0rezct6qA4GZ0FZ0Ez1MGvGvQXDgm0-aGelMyGVl-X1M3oQvVIoeWl5-I5SCcifMdvO0U7o91a-Nn-FWcC73ieX4NJRBH2mpY6hZzB55enkSp0r893135VNNan4_Lra-P9XiRvfDeKQRwWySkUm6aUf4yOtXfTYnnCw1sfsxrEjxQKT29evG-fo8Wx134B7yLmTlPr5ar7AR-7xZhEHA6YppXPwof9N1_0MiVMiRSrecO8D-321ULkKaCYHR0piTrfjcNDmLKtZqmfaBsG-ri-uxWCKroB8dKiXGFpKpdExYIgpMYQFu9kAubYwzv0yzroeu_JzwjFiVgqa1bh8PGFvCqAypFyuEmutCpVVYYQO6kvWx_xXRi8jjolMeJt59V9jIdvXadfshC3E2hRFrA-rHVjEzX8uKbizwipYoln3_A1shuoCbGQGlh4kUwrxMdnLlVUbhUJONMVN5MA-uak8zM2EZDSA_SSAkwUY1DCQQr5JkrZXRa-gZjzJ49rPfB_Gnlid4wDNgJ33slTduariz0pGVgMGYPZqAYDloDZs8o7Ty5luvX6aKjGkVlxU0K0)
[Source code](https://www.planttext.com/?text=ZLLTJzim57tth_1ZDXN-05M5sWsK8LZLT4_J7enp2PPSExDlqaN0VyTs7PPFuCsvFZxtd7Fjzj8YCrWT924eK0BDX2egoOp0vWwG_c0rezct6qA4GZ0FZ0Ez1MGvGvQXDgm0-aGelMyGVl-X1M3oQvVIoeWl5-I5SCcifMdvO0U7o91a-Nn-FWcC73ieX4NJRBH2mpY6hZzB55enkSp0r893135VNNan4_Lra-P9XiRvfDeKQRwWySkUm6aUf4yOtXfTYnnCw1sfsxrEjxQKT29evG-fo8Wx134B7yLmTlPr5ar7AR-7xZhEHA6YppXPwof9N1_0MiVMiRSrecO8D-321ULkKaCYHR0piTrfjcNDmLKtZqmfaBsG-ri-uxWCKroB8dKiXGFpKpdExYIgpMYQFu9kAubYwzv0yzroeu_JzwjFiVgqa1bh8PGFvCqAypFyuEmutCpVVYYQO6kvWx_xXRi8jjolMeJt59V9jIdvXadfshC3E2hRFrA-rHVjEzX8uKbizwipYoln3_A1shuoCbGQGlh4kUwrxMdnLlVUbhUJONMVN5MA-uak8zM2EZDSA_SSAkwUY1DCQQr5JkrZXRa-gZjzJ49rPfB_Gnlid4wDNgJ33slTduariz0pGVgMGYPZqAYDloDZs8o7Ty5luvX6aKjGkVlxU0K0)

## How to build

Install dependencies for the first time:
```
curl -L https://get.web3j.io | bash
source $HOME/.web3j/source.sh
./get_contracts.sh
```

Build and test:
```
mvn test
```

