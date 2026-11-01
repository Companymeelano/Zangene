<?php

return [

    /*
    |--------------------------------------------------------------------------
    | v6.8 · SMS gateway
    |--------------------------------------------------------------------------
    | driver: kavenegar | mellipayamak | smsir | ghasedak | sandbox
    | The merchant can override every value at runtime from the admin panel;
    | these are only the boot defaults.
    */
    'sms' => [
        'driver' => env('SMS_DRIVER', 'sandbox'),
        'enabled' => env('SMS_ENABLED', true),
        'sender' => env('SMS_SENDER', ''),
        'store_name' => env('SMS_STORE_NAME', 'اسیه استور'),
        'owner_name' => env('SMS_OWNER_NAME', 'خانم زنگنه'),
        'owner_mobile' => env('SMS_OWNER_MOBILE', '09044200993'),

        'kavenegar' => [
            'key' => env('KAVENEGAR_KEY', ''),
        ],
        'mellipayamak' => [
            'username' => env('MELLIPAYAMAK_USERNAME', ''),
            'password' => env('MELLIPAYAMAK_PASSWORD', ''),
        ],
        'smsir' => [
            'key' => env('SMSIR_KEY', ''),
        ],
        'ghasedak' => [
            'key' => env('GHASEDAK_KEY', ''),
        ],
    ],
];
