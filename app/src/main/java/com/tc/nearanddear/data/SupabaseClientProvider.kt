package com.tc.nearanddear.data

import com.tc.nearanddear.MainActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {

    lateinit var client: SupabaseClient
        private set

    fun initialize(application: MainActivity) {
        client = createSupabaseClient(
            supabaseUrl = "https://bbdadykysfweivoqcrap.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJiZGFkeWt5c2Z3ZWl2b3FjcmFwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMwNzQ1MzMsImV4cCI6MjA1ODY1MDUzM30.VLJiw_CcFT54PZHQzyW_du8gno6NZshu80O8tYkgbLA"
        ) {
            install(Auth)
            install(Postgrest)
            //install other modules
        }
    }
}