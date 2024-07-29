package org.toptaxi.taximeter.tools

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.tinkoff.acquiring.sdk.redesign.mainform.MainFormLauncher
import ru.tinkoff.acquiring.sdk.redesign.sbp.SbpPayLauncher

open class MainAppCompatActivityKt : AppCompatActivity() {
    val mainFormPaymentLauncher =
        registerForActivityResult(MainFormLauncher.Contract) { _ -> }
    val mainFormSBPPaymentLauncher =
        registerForActivityResult(SbpPayLauncher.Contract) { _ -> }



}