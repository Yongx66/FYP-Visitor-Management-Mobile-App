<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Http\Response;
use Hash;
use Validator;
use Carbon\Carbon;
use App\Models\User;
use App\Helpers\CommonHelper;

class LoginController extends Controller
{
    public function login(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'email' => 'required|string',
            'password' => 'required|string'
        ]);

        if ($validator->fails()) {
            return CommonHelper::commonResponse(false, Response::HTTP_BAD_REQUEST, $validator->messages());
        }

        $loggedinUser = User::where('email', $request->email)->first();

        if(isset($loggedinUser) && Hash::check($request->password, $loggedinUser->password)) {
            $loggedinUser->last_ip = $request->ip();
            $loggedinUser->last_login = Carbon::now();
            $loggedinUser->save();

            $token = $loggedinUser->createToken(env('SANCTUM_TOKEN_NAME'))->plainTextToken;

            return CommonHelper::commonResponse(true, Response::HTTP_OK, $token);
        }

        return CommonHelper::commonResponse(false, Response::HTTP_BAD_REQUEST, "invalid login credentials");
    }

    public function logout(Request $request)
    {
        if ($request->user()) {
            $request->user()->tokens->each(function ($token) {
                $token->delete();
            });
            
            return CommonHelper::commonResponse(true, Response::HTTP_OK, "logout successful");
        }

        return CommonHelper::commonResponse(false, Response::HTTP_BAD_REQUEST, "user token not found");
    }

    public function me(Request $request)
    {
        if ($request->user()) { 
            $info['user_info'] = $request->user();
            $info['user_token_info'] = $request->bearerToken();
            $info['user_role_info'] = $request->user()->getRoleNames()[0];
            return CommonHelper::commonResponse(true, Response::HTTP_OK, $info);
        }

        return CommonHelper::commonResponse(false, Response::HTTP_BAD_REQUEST, "user token not found");
    }
}
