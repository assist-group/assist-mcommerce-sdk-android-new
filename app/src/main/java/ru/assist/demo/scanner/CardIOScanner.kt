package ru.assist.demo.scanner

import android.content.Context
import android.content.Intent
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard
import kotlinx.parcelize.Parcelize
import ru.assist.sdk.scanner.CardData
import ru.assist.sdk.scanner.CardScanner

@Parcelize
class CardIOScanner: CardScanner() {
	override fun getScannerIntent(context: Context) =
		Intent(context, CardIOActivity::class.java)
			.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true)

	override fun getCardDataFromIntent(data: Intent) =
		if (data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
			val scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT) as? CreditCard
			val month = (scanResult?.expiryMonth ?: 0).toString().padStart(2, '0')
			val yearString = scanResult?.expiryYear?.toString() ?: "00"
			val year = if (yearString.length > 2) {
				yearString.substring(yearString.lastIndex - 1)
			} else {
				yearString.padStart(2, '0')
			}
			CardData(scanResult?.cardNumber, month, year, scanResult?.cardholderName)
		} else {
			null
		}
}