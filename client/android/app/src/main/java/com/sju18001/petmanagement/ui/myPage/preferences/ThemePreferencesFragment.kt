package com.sju18001.petmanagement.ui.myPage.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.sju18001.petmanagement.R

class ThemePreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey)
    }
}