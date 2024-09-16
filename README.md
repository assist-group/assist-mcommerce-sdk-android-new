## Assist Mobile SDK

SDK позволяет проводить платежи через платёжный шлюз Ассист.

### Возможности

- оплата картой через WebView;
- оплата через Google Pay, Samsung Pay или Mir Pay;
- поддержка Системы Быстрых Платежей;
- оплата по ссылке;
- сканирование карты для оплаты;
- 2 режима работы: получение ответа в собственный listener или в ActivityResult;
- журнал заказов.

### Требования

Android версии 7.0 или выше (API level 24).

### Подключение

В build.gradle уровня проекта добавить репозиторий Jitpack
```
repositories {
    maven { url 'https://jitpack.io' }
}
```
В build.gradle уровня приложения добавить зависимость, указав последнюю доступную версию SDK:

[![](https://jitpack.io/v/assist-group/assist-mcommerce-sdk-android-new.svg)](https://jitpack.io/#assist-group/assist-mcommerce-sdk-android-new)

```
implementation 'com.github.assist-group:assist-mcommerce-sdk-android-new:latest-release'
```

### Структура проекта

- **app** - пример реализации приложения, использующего SDK;
- **sdk** - исходный код SDK.

### Подготовка к работе

Для проведения платежей (как тестовых, так и боевых) необходимо получить следующие данные у [поддержки Ассист](mailto:support@assist.ru):

- merchant ID;
- server URL;
- login;
- password.

Инициализация Assist SDK проходит следующим образом:
```kotlin
val config = Configuration(
    apiURL = "https://payments.paysecure.ru", // server URL
    link = null, // ссылка на готовый заказ
)
val sdk = AssistSDK.getInstance().configure(context, config)
```
Методы, доступные в Assist SDK:
```kotlin
sdk.payWeb(context, data, scanner, allowRedirect, ::processResult) // Оплата через WebView
sdk.payToken(context, data, token, type, ::processResult) // Оплата токеном GooglePay, SamsungPay или MirPay
sdk.declineByNumber(context, data, ::processResult) // Отказ от заказа (прерывание оплаты)
sdk.getOrderDataByNumber(context, order, ::processResult) // Получение данных заказа по номеру заказа
sdk.getOrderDataByLink(link, ::processResult) // Получение данных заказа по ссылке
sdk.getOrdersFromStorage() // Получение заказов из журнала заказов
sdk.deleteOrderInStorage(order) // Удаление заказа из журнала заказов
```

### Оплата через WebView

```kotlin
sdk.payWeb(context, data, scanner, ::processResult, allowRedirect)
```
- **context** - контекст текущей activity или приложения;
- **data** - объект [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt),
минимальный набор полей для оплаты: merchantID, orderNumber, orderAmount;
полный набор в [документации](https://docs.assist.ru/pages/viewpage.action?pageId=5767488);
- **scanner** - объект [CardScanner](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/scanner/CardScanner.kt) (необязательный, default=null);
если он не null, то при запуске WebView сразу откроется сканер карт;
- **allowRedirect** - флаг, будет ли вызван внешний сервис по окончанию платежа (необязательный, default=false).
Если у вас включён редирект после окончания платежа, то необходимо передавать allowRedirect=true.
Визуально в webView редиректа не будет, результат платежа по-прежнему вернётся объектом AssistResult.
- **::processResult** - метод приложения processResult(result: AssistResult), в который приходит результат платежа;

Данный метод имеет Intent-версию (про Intent-сценарий подробнее будет ниже):
```kotlin
val intent = sdk.createPayWebIntent(context, data, scanner, allowRedirect)
startActivityForResult(intent, assistRequestCode)
```

### Оплата через GooglePay, SamsungPay или MirPay

```kotlin
sdk.payToken(context, data, token, type, ::processResult)
```
- **context** - контекст текущей activity или приложения;
- **data** - объект [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt),
минимальный набор полей для оплаты: merchantID, orderNumber, orderAmount;
полный набор в [документации](https://docs.assist.ru/pages/viewpage.action?pageId=5767488);
- **token** - платёжный токен GooglePay, SamsungPay или MirPay;
- **type** - объект [PaymentTokenType](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/models/PaymentTokenType.kt),
указывающий, чей это токен (GooglePay, SamsungPay или MirPay);
- **::processResult** - метод приложения processResult(result: AssistResult), в который приходит результат платежа.

Платёжный токен формируется на стороне клиентского приложения. Для Samsung и Mir для этого необходимо использовать их paySdk
и соответствующее приложение. Для Google SDK не нужен, только приложение. Примеры получения токенов вы можете
увидеть в коде тестового приложения в
[ru.assist.demo.pays](https://github.com/assist-group/assist-mcommerce-sdk-android-new/tree/main/app/src/main/java/ru/assist/demo/pays)
и [ru.assist.demo.ui.MainActivity](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/app/src/main/java/ru/assist/demo/ui/MainActivity.kt).

Также рекомендуется ознакомиться с документацией на сайте разработчика:
- GooglePay [https://developers.google.com/pay](https://developers.google.com/pay);
- SamsungPay [http://www.samsung.com/ru/apps/mobile/samsungpay/](http://www.samsung.com/ru/apps/mobile/samsungpay/).

Данный метод имеет Intent-версию (про Intent-сценарий подробнее будет ниже):
```kotlin
val intent = sdk.createPayTokenIntent(context, data, token, type)
startActivityForResult(intent, assistRequestCode)
```

**MirPay Deep Link**

У MirPay есть возможность формирования токена без использования mirPaySdk - через Deep Link.
Корректное формирование Deep Link можно посмотреть в классе [ru.assist.demo.pays.MirPay](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/app/src/main/java/ru/assist/demo/pays/MirPay.kt).
В данном сценарии ответ с токеном придёт на указанный в диплинке endpoint.
Для дальнейшей оплаты необходимо будет также передать его в метод **payToken**.

### Система быстрых платежей

Необходимо указать в объекте [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt)
поле **fastPayPayment="1"** и использовать метод **payWeb**.

### Отказ от заказа

```kotlin
sdk.declineByNumber(context, data, ::processResult)
```
- **context** - контекст текущей activity или приложения;
- **data** - объект [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt),
минимальный набор полей для отказа: merchantID, orderNumber, orderAmount;
- **::processResult** - метод приложения processResult(result: AssistResult), в который приходит результат платежа.

Если заказ создан, но по какой-то причине необходимо прервать оплату, то методом **declineByNumber** его можно завершить принудительно.

Данный метод имеет Intent-версию (про Intent-сценарий подробнее будет ниже):
```kotlin
val intent = sdk.createDeclineByNumberIntent(context, data)
startActivityForResult(intent, assistRequestCode)
```

### Intent-сценарий

Если вам удобнее получать результат платежа в **onActivityResult**, то необходимо вызывать специальные методы оплаты.
Например, вместо **payWeb** необходимо использовать:
```kotlin
val intent = sdk.createPayWebIntent(context, data, scanner, allowRedirect)
startActivityForResult(intent, assistRequestCode) // или registerForActivityResult
  //
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  if (requestCode == assistRequestCode) {
    (data?.getParcelableExtra(PayActivity.EXTRA_ASSIST_RESULT) as? AssistResult)?.let {
      // Если resultCode=RESULT_OK, то возвращается AssistResult.result с ответом сервера Assist
      // Если resultCode=RESULT_CANCELED, то возвращается AssistResult.msg с текстом ошибки
      processResult(it)
    }
  }
}
```
Пример реализации можно посмотреть в классе
[ru.assist.demo.ui.MainActivity](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/app/src/main/java/ru/assist/demo/ui/MainActivity.kt).

### Получение данных заказа по номеру заказа

```kotlin
sdk.getOrderDataByNumber(context, order, ::processResult)
```
- **context** - контекст текущей activity или приложения;
- **order** - объект [AssistResult](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/models/AssistResult.kt),
который был получен в результате платежа, минимальный набор полей для получения данных: result.merchantId, result.orderNumber, result.dateMillis;
- **::processResult** - метод приложения processResult(result: AssistResult), в который приходят данные заказа.

Метод **getOrderDataByNumber** пригодится для обновления статуса заказа, если по каким-то причинам
после оплаты через **payWeb** или **payToken** он ещё "в процессе".

### Оплата по ссылке

Заказ можно сформировать заранее, чтобы в приложении не было необходимости заполнять нужные поля.
Для этого необходимо использовать [сервис payrequest](https://docs.assist.ru/pages/viewpage.action?pageId=17368487),
который выдаст ссылку на готовый к оплате заказ.
Если перед оплатой необходимо вывести данные заказа, следует использовать метод:
```kotlin
sdk.getOrderDataByLink(link, ::processResult)
```
- **link** - URL заказа, полученный от сервиса payrequest;
- **::processResult** - метод приложения processResult(result: AssistResult), в который приходят данные заказа.

Также этот метод пригодится для получения платёжного токена *Pay, т.к. формировать его надо как раз из данных заказа.

Далее необходимо передать **link** в конфигурацию SDK:
```kotlin
val config = Configuration(
    apiURL = "https://payments.paysecure.ru", // server URL
    link = "https://payments.paysecure.ru/pay/pay.cfm?CFSID=ABCD&stage=iframe", // ссылка на готовый заказ
)
val sdk = AssistSDK.getInstance().configure(context, config)
```
И оплатить методом **payWeb** или **payToken**, где в [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt)
необходимо заполнить только merchantID, login и password.

### Фискализация

Для включения фискализации необходимо связаться с [поддержкой Ассист](mailto:support@assist.ru).
Фискальные данные необходимо передавать в объекте [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt).
Описание полей можно найти в [документации](https://docs.assist.ru/pages/viewpage.action?pageId=5768155).

### Журнал заказов

Assist SDK имеет встроенную БД, где хранятся все проведённые заказы. По умолчанию хранение включено (storageEnabled = true).
Чтобы выключить журнал заказов, необходимо при конфигурации SDK передать параметр
```kotlin
storageEnabled = false
```
Методы для работы с журналом заказов:
```kotlin
val list = sdk.getOrdersFromStorage() // Получение заказов в виде List<AssistResult>
sdk.deleteOrderInStorage(order) // Удаление указанного AssistResult из журнала заказов
```

### Поддержка

По всем вопросам и багам обращайтесь в поддержку.
Служба поддержки Ассист [support@assist.ru](mailto:support@assist.ru)


## Assist Mobile SDK

The SDK allows you to make payments through the Assist payment gateway.

### Features

- payment by card via WebView;
- payment via Google Pay, Samsung Pay or Mir Pay;
- the Faster Payment System (SBP) support;
- payment via link;
- scanning a card for payment;
- 2 operating modes: receiving a response in your own Listener or in ActivityResult;
- order log.

### Requirements

Android version 7.0 or higher (API level 24).

### Connection

Add Jitpack repository to project level build.gradle
```
repositories {
maven { url 'https://jitpack.io' }
}
```
In the application level build.gradle, add the dependency, specifying the latest available SDK version:

[![](https://jitpack.io/v/assist-group/assist-mcommerce-sdk-android-new.svg)](https://jitpack.io/#assist-group/assist-mcommerce-sdk-android-new)

```
implementation 'com.github.assist-group:assist-mcommerce-sdk-android-new:latest-release'
```

### Project structure

- **app** - example implementation of an application using SDK;
- **sdk** - SDK source code.

### Preparing for work

To make payments (both test and live), you must obtain the following data from [Assist support team](mailto:support@assist.ru):
- merchant ID;
- server URL;
- login;
- password.

The Assist SDK is initialized as follows:
```kotlin
val config = Configuration(
    apiURL = "https://payments.paysecure.ru", // server URL
    link = null, // link to prepared order
)
val sdk = AssistSDK.getInstance().configure(context, config)
```
Methods available in Assist SDK:
```kotlin
sdk.payWeb(context, data, scanner, allowRedirect, ::processResult) // Payment via WebView
sdk.payToken(context, data, token, type, ::processResult) // Payment with Google Pay, Samsung Pay or MirPay token
sdk.declineByNumber(context, data, ::processResult) // Order cancellation (interruption of payment)
sdk.getOrderDataByNumber(context, order, ::processResult) // Receiving order data by order number
sdk.getOrderDataByLink(link, ::processResult) // Receiving order data using a link
sdk.getOrdersFromStorage() // Receiving orders from the order log
sdk.deleteOrderInStorage(order) // Removing an order from the order log
```

### Payment via WebView

```kotlin
sdk.payWeb(context, data, scanner, allowRedirect, ::processResult)
```
- **context** - context of the current activity or application;
- **data** - the object [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt), minimum set of fields for payment: merchantID, orderNumber, orderAmount; full set of fields for payment see in [the documentation](https://docs.assist.ru/pages/viewpage.action?pageId=5767488);
- **scanner** - the object [CardScanner](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/scanner/CardScanner.kt) (not required, default=null); if it is not null, then by the WebView start, the map scanner will immediately open;
- **allowRedirect** - the flag whether the external service will be called after the payment is completed (not required, default=false).
If you have enabled redirect after the end of the payment, you should pass allowRedirect=true.
Visually in the webView there will be no redirect, the payment result will still be returned by the AssistResult object.
- **::processResult** - application method processResult(result: AssistResult), which receives the payment result.

This method has an Intent version (the Intent-scenario is described detailed below):
```kotlin
val intent = sdk.createPayWebIntent(context, data, scanner, allowRedirect)
startActivityForResult(intent, assistRequestCode)
```

### Payment via Google Pay, Samsung Pay or MirPay

```kotlin
sdk.payToken(context, data, token, type, ::processResult)
```
- **context** - context of the current activity or application;
- **data** - the object [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt), minimum set of fields for payment: merchantID, orderNumber, orderAmount; full set of fields for payment see in [the documentation](https://docs.assist.ru/pages/viewpage.action?pageId=5767488);
- **token** - payment token GooglePay, SamsungPay or MirPay;
- **type** - the object [PaymentTokenType](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/models/PaymentTokenType.kt) indicating whose token it is - GooglePay, SamsungPay or MirPay;
- **::processResult** - application method processResult(result: AssistResult), which receives the payment result.

The payment token is generated on the client application side - however, for Samsung and Mir,
you need to use their paySdk and the corresponding application, but for Google the SDK is not needed,
you only need the application. You can see examples of receiving tokens in the test application code in
[ru.assist.demo.pays](https://github.com/assist-group/assist-mcommerce-sdk-android-new/tree/main/app/src/main/java/ru/assist/demo/pays)
and [ru.assist.demo.ui.MainActivity](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/app/src/main/java/ru/assist/demo/ui/MainActivity.kt).

It is also recommended to read the documentation on the developer's website:
- GooglePay [https://developers.google.com/pay](https://developers.google.com/pay);
- SamsungPay [http://www.samsung.com/ru/apps/mobile/samsungpay/](http://www.samsung.com/ru/apps/mobile/samsungpay/).

This method has an Intent version (the Intent-scenario is described detailed below):
```kotlin
val intent = sdk.createPayTokenIntent(context, data, token, type)
startActivityForResult(intent, assistRequestCode)
```

**MirPay Deep Link**

Mir Pay can generate tokens without using mirPaySdk - through Deep Link. Correct generation of Deep Link can be seen in the class
[ru.assist.demo.pays.MirPay](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/app/src/main/java/ru/assist/demo/pays/MirPay.kt).
In this scenario, the response with the token will be sent to the endpoint specified in the Deep Link. For further payment, you will also need to transfer it to the **payToken** method.

### Faster Payment System

Must be specified in the [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt) object field **fastPayPayment="1"** and use the **payWeb** method.

### Order cancellation

```kotlin
sdk.declineByNumber(context, data, ::processResult)
```
- **context** - context of the current activity or application;
- **data** - the object [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt), minimum set of fields for payment: merchantID, orderNumber, orderAmount;
- **::processResult** - application method processResult(result: AssistResult), which receives the payment result.
 
If an order has been created, but for some reason it is necessary to interrupt payment, then it can be completed forcibly using the **declineByNumber** method.

This method has an Intent version (the Intent-scenario is described detailed below):
```kotlin
val intent = sdk.createDeclineByNumberIntent(context, data)
startActivityForResult(intent, assistRequestCode)
```

### Intent-scenario

If it is more convenient for you to receive the payment result in **onActivityResult**,
then you need to call special payment methods. For example, instead of **payWeb** you need to use:
```kotlin
val intent = sdk.createPayWebIntent(context, data, scanner, allowRedirect)
startActivityForResult(intent, assistRequestCode) // or registerForActivityResult
  //
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  if (requestCode == assistRequestCode) {
    (data?.getParcelableExtra(PayActivity.EXTRA_ASSIST_RESULT) as? AssistResult)?.let {
      // If resultCode=RESULT_OK, then AssistResult.result is returned with the Assist server response
      // If resultCode=RESULT_CANCELED, then AssistResult.msg is returned with the error text
      processResult(it)
    }
  }
}
```
An example of implementation you can see in the class [ru.assist.demo.ui.MainActivity](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/app/src/main/java/ru/assist/demo/ui/MainActivity.kt).

### Receiving order data by order number

```kotlin
sdk.getOrderDataByNumber(context, order, ::processResult)
```
- **context** - context of the current activity or application;
- **order** - object [AssistResult](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/models/AssistResult.kt) that was received as a result of payment, minimum set of fields for payment: result.merchantId, result.orderNumber, result.dateMillis;
- **::processResult** - application method processResult(result: AssistResult), which receives the payment result.

The **getOrderDataByNumber** method is useful for updating the order status if for some reason after payment via **payWeb** or **payToken** it is still “In progress”.

### Payment via link

The order can be generated in advance so that you do not need to fill out the required fields in the application. To do this, you need to use
[payrequest service](https://docs.assist.ru/pages/viewpage.action?pageId=17368487), which will provide a link to the order ready for payment.
If you need to display order details before payment, you should use the method:
```kotlin
sdk.getOrderDataByLink(link, ::processResult)
```
- **link** - order URL received from payrequest service;
- **::processResult** - application method processResult(result: AssistResult), which receives the payment result.

This method is also useful for receiving the *Pay payment token, because it must be generated from the order data.

Next you need to set **link** in the SDK configuration:
```kotlin
val config = Configuration(
    apiURL = "https://payments.paysecure.ru", // server URL
    link = "https://payments.paysecure.ru/pay/pay.cfm?CFSID=ABCD&stage=iframe", // link to prepared order
)
val sdk = AssistSDK.getInstance().configure(context, config)
```
And pay using the **payWeb** or **payToken** method, where in
[AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt)
you only need to fill in merchantID, login and password fields.

### Fiscalization

To enable fiscalization, you must contact [Assist support team](mailto:support@assist.ru).
Fiscal data must be transferred in a [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt) object. Description of the fields you can found in [documentation](https://docs.assist.ru/pages/viewpage.action?pageId=5768155).

### Order log

Assist SDK has a built-in database where all completed orders are stored. By default, storage is enabled (storageEnabled = true).
To disable the order log, you must set the parameter when configuring the SDK:
```kotlin
storageEnabled = false
```
Methods for working with the order log:
```kotlin
val list = sdk.getOrdersFromStorage() // Receive orders as List<AssistResult>
sdk.deleteOrderInStorage(order) // Delete the specified AssistResult from the order log
```

### Support

For any questions or bugs, please contact support.
Assist support service [support@assist.ru](mailto:support@assist.ru)
