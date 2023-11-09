## Assist Mobile SDK

SDK позволяет проводить платежи через платёжный шлюз Ассист.

### Возможности

- Оплата картой через WebView
- Оплата через Google Pay, Samsung Pay или Mir Pay
- Поддержка Системы Быстрых Платежей
- Оплата по ссылке
- Сканирование карты для оплаты
- 2 режима работы: получение ответа в собственный listener или в ActivityResult
- Журнал заказов

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

- **app** - пример реализации приложения, использующего SDK
- **sdk** - исходный код SDK

### Подготовка к работе

Для проведения платежей (как тестовых, так и боевых) необходимо получить следующие данные у [поддержки Ассист](mailto:support@assist.ru):

- merchant ID
- server URL
- login
- password

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
sdk.payWeb(context, data, scanner, ::processResult) // Оплата через WebView
sdk.payToken(context, data, token, type, ::processResult) // Оплата токеном GooglePay, SamsungPay или MirPay
sdk.declineByNumber(context, data, ::processResult) // Отказ от заказа (прерывание оплаты)
sdk.getOrderDataByNumber(context, order, ::processResult) // Получение данных заказа по номеру заказа
sdk.getOrderDataByLink(link, ::processResult) // Получение данных заказа по ссылке
sdk.getOrdersFromStorage() // Получение заказов из журнала заказов
sdk.deleteOrderInStorage(order) // Удаление заказа из журнала заказов
```

### Оплата через WebView

```kotlin
sdk.payWeb(context, data, scanner, ::processResult)
```
- **context** - контекст текущей activity или приложения
- **data** - объект [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt),
минимальный набор полей для оплаты: merchantID, login, password, orderNumber, orderAmount.
Полный набор в [документации](https://docs.assist.ru/pages/viewpage.action?pageId=5767488).
- **scanner** - объект [CardScanner](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/scanner/CardScanner.kt).
Если он не null, то при запуске WebView сразу откроется сканер карт
- **::processResult** - метод приложения processResult(result: AssistResult), в который приходит результат платежа

Данный метод имеет Intent-версию (про Intent-сценарий подробнее будет ниже):
```kotlin
val intent = sdk.createPayWebIntent(context, data, scanner)
startActivityForResult(intent, assistRequestCode)
```

### Оплата через GooglePay, SamsungPay или MirPay

```kotlin
sdk.payToken(context, data, token, type, ::processResult)
```
- **context** - контекст текущей activity или приложения
- **data** - объект [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt),
минимальный набор полей для оплаты: merchantID, login, password, orderNumber, orderAmount.
Полный набор в [документации](https://docs.assist.ru/pages/viewpage.action?pageId=5767488).
- **token** - платёжный токен GooglePay, SamsungPay или MirPay
- **type** - объект [PaymentTokenType](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/models/PaymentTokenType.kt),
указывающий, чей это токен (GooglePay, SamsungPay или MirPay)
- **::processResult** - метод приложения processResult(result: AssistResult), в который приходит результат платежа

Платёжный токен формируется на стороне клиентского приложения. Для Samsung и Mir для этого необходимо использовать их paySdk
и соответствующее приложение. Для Google SDK не нужен, только приложение. Примеры получения токенов вы можете
увидеть в коде тестового приложения в
[ru.assist.demo.pays](https://github.com/assist-group/assist-mcommerce-sdk-android-new/tree/main/app/src/main/java/ru/assist/demo/pays)
и [ru.assist.demo.ui.MainActivity](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/app/src/main/java/ru/assist/demo/ui/MainActivity.kt).

Так же рекомендуется ознакомиться с документацией на сайте разработчика:
- GooglePay [https://developers.google.com/pay](https://developers.google.com/pay)
- SamsungPay [http://www.samsung.com/ru/apps/mobile/samsungpay/](http://www.samsung.com/ru/apps/mobile/samsungpay/)

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
- **context** - контекст текущей activity или приложения
- **data** - объект [AssistPaymentData](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/api/models/AssistPaymentData.kt),
минимальный набор полей для отказа: merchantID, login, password, orderNumber
- **::processResult** - метод приложения processResult(result: AssistResult), в который приходит результат платежа

Если заказ создан, но по какой-то причине необходимо прервать оплату, то методом declineByNumber его можно завершить принудительно.

Данный метод имеет Intent-версию (про Intent-сценарий подробнее будет ниже):
```kotlin
val intent = sdk.createDeclineByNumberIntent(context, data)
startActivityForResult(intent, assistRequestCode)
```

### Intent-сценарий

Если вам удобнее получать результат платежа в **onActivityResult**, то необходимо вызывать специальные методы оплаты.
Например вместо **payWeb** необходимо использовать:
```kotlin
val intent = sdk.createPayWebIntent(context, data, scanner)
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
- **context** - контекст текущей activity или приложения
- **order** - объект [AssistResult](https://github.com/assist-group/assist-mcommerce-sdk-android-new/blob/main/sdk/src/main/java/ru/assist/sdk/models/AssistResult.kt),
который был получен в результате платежа, минимальный набор полей для получения данных: result.merchantId, result.orderNumber, result.dateMillis
- **::processResult** - метод приложения processResult(result: AssistResult), в который приходят данные заказа

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
- **link** - URL заказа, полученный от сервиса payrequest
- **::processResult** - метод приложения processResult(result: AssistResult), в который приходят данные заказа

Так же этот метод пригодится для получения платёжного токена *Pay, т.к. формировать его надо как раз из данных заказа.

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