<?php

namespace App\Helpers;

class CommonHelper {
    public static function commonResponse($status, $code, $message) {
        $return["status"] = $status;
        $return["errCode"] = $code;
        $return["message"] = $message;

        return response()->json($return, $code);
    }

    public static function numbersOnly($string)
    {
        return preg_replace('/\D/', '', $string);
    }
}