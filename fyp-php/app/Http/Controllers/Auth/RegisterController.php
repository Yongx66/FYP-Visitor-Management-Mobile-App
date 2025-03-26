<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\User;
use Illuminate\Validation\Rules\Password;
use Validator;
use App\Helpers\CommonHelper;
use Illuminate\Http\Response;

class RegisterController extends Controller 
{
    public function register(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'email' => ['required', 'email:rfc,dns', 'unique:users,email'],
            'password' => [
                'confirmed',
                'required',
                Password::min(6)        // must be at least 6 characters in length
                    ->letters()         // must be at least one letter
                    ->numbers()         // must contain at least one digit
            ],
            'user_type' => ['required', 'in:1,2'] // 1: ADMIN 2: USER
        ]);

        if ($validator->fails()) {
            return CommonHelper::commonResponse(false, Response::HTTP_BAD_REQUEST, $validator->messages());
        }

        $request['password'] = bcrypt($request->password);

        // insert new user into database
        $user = User::create($request->all());
        if ($request->user_type == 1) {
            $user->assignRole('ADMIN');
        } else if ($request->user_type == 2) {
            $user->assignRole('USER');
        }

        return CommonHelper::commonResponse(true, Response::HTTP_OK, "register successful as user type of ".$request->user_type);
    }
}