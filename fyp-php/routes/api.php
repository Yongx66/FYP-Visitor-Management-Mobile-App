<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Auth\RegisterController;
use App\Http\Controllers\Auth\LoginController;
use App\Http\Controllers\VisitorController;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Here is where you can register API routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| is assigned the "api" middleware group. Enjoy building your API!
|
*/

Route::middleware('auth:api')->get('/user', function (Request $request) {
    return $request->user();
});

Route::prefix('user')->group(function () { 
    Route::post('/register', [RegisterController::class, 'register']);
    Route::post('/login', [LoginController::class, 'login']);

    Route::group(['middleware' => ['auth:sanctum']], function () {
        Route::post('/logout', [LoginController::class, 'logout']);
        Route::get('/me', [LoginController::class, 'me']);
    });
});

Route::group(['middleware' => ['auth:sanctum']], function () {
    Route::prefix('visitor')->group(function() {
        Route::post('/register', [VisitorController::class, 'register']);
        Route::get('/self/records', [VisitorController::class, 'listOfSelfRecords']);

        Route::get('/all/records', [VisitorController::class, 'listOfAllRecords'])->middleware("role:SUPERADMIN|ADMIN");
        Route::get('/verify/record/{token}', [VisitorController::class, 'verifyRecord'])->middleware("role:SUPERADMIN|ADMIN");
    });
});