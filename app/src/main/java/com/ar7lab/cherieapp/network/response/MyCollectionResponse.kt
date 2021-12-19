package com.ar7lab.cherieapp.network.response

import com.ar7lab.cherieapp.model.MyCollectionData

class MyCollectionResponse(
    var status: String,
    var message: String,
    var data: MyCollectionData
)