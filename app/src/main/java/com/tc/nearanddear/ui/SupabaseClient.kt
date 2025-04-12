package com.tc.nearanddear.ui

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {
    private const val TAG = "SupabaseClient" // Log tag for listener logs

    private val client = createSupabaseClient(
        supabaseUrl = "https://bbdadykysfweivoqcrap.supabase.co", // Replace with your Supabase URL
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJiZGFkeWt5c2Z3ZWl2b3FjcmFwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMwNzQ1MzMsImV4cCI6MjA1ODY1MDUzM30.VLJiw_CcFT54PZHQzyW_du8gno6NZshu80O8tYkgbLA" // Replace with your Supabase anon key
    ) {
        install(Postgrest)
        install(Realtime)
    }
}