<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Validator;
use App\Helpers\CommonHelper;
use Illuminate\Http\Response;
use App\Models\User;
use App\Models\VisitorRecord;
use Str;
use Auth;

class VisitorController extends Controller
{
    public function register(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'full_name' => 'required|regex:/^[a-zA-Z ]+$/u|max:255',
            'contact_no' => 'required|numeric',
            'date_of_visit' => 'required',
            'reason_of_visiting' => 'required|string'
        ]);

        if ($validator->fails()) {
            return CommonHelper::commonResponse(false, Response::HTTP_BAD_REQUEST, $validator->messages());
        }

        $user = Auth::user();

        // check if there is an existing visitor record equals to the date of visit
        $active_visit_record_count = $user->self_active_visiting_record($request->date_of_visit)->count();
        if ($active_visit_record_count > 0) { // block user from submitting again the same date of visit record
            return CommonHelper::commonResponse(false, Response::HTTP_BAD_REQUEST, "A visitor record of the same date is already exist");
        }

        // update logged-in user's full name & contact no.
        $user->update([
            'full_name' => $request->full_name,
            'contact_no' => CommonHelper::numbersOnly($request->contact_no),
        ]);

        $request['user_id'] = $user->id;
        $request['token'] = Str::random(60);
        
        // insert visitor record
        VisitorRecord::create($request->all());

        return CommonHelper::commonResponse(true, Response::HTTP_OK, "Visitor record was created successfully");
    }

    public function listOfSelfRecords(Request $request)
    {
        if ($request->user()) {
            $user = Auth::user();
            
            $record["self_active_visiting_record"] = $user->self_active_visiting_record(date('Y-m-d'))->get();
            $record["self_incoming_visiting_record"] = $user->self_incoming_visiting_record(date('Y-m-d'))->get();
            $record["self_past_visited_records"] = $user->self_past_visited_records(date('Y-m-d'))->get();

            return CommonHelper::commonResponse(true, Response::HTTP_OK, $record);
        }

        return CommonHelper::commonResponse(false, Response::HTTP_BAD_REQUEST, "user token not found");
    }

    public function listOfAllRecords()
    {
        $visitor_records = VisitorRecord::all();
        $all_records = array();
        foreach($visitor_records as $record) {
            $tmp_record['visitor_record'] = $record;
            $tmp_record['visitor_info'] = $record->user->select('full_name', 'contact_no')->where('id', $record->user_id)->get();
            
            array_push($all_records, $tmp_record);
        }

        if (count($all_records) > 0) {
            return CommonHelper::commonResponse(true, Response::HTTP_OK, $all_records);
        }

        return CommonHelper::commonResponse(true, Response::HTTP_OK, "No visitor records found");
    }

    public function verifyRecord($token)
    {
        $record = VisitorRecord::where([
            ['token', $token],
            ['date_of_visit', date('Y-m-d')]
        ])->get();

        if (count($record) > 0) {
            return CommonHelper::commonResponse(true, Response::HTTP_OK, $record);
        }

        return CommonHelper::commonResponse(false, Response::HTTP_BAD_REQUEST, "Invalid QR code");
    }
}
