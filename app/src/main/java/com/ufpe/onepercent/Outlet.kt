package com.ufpe.onepercent

class Outlet(val place: String, val description: String) {
    init {
        require(place.trim().length > 0) {
            "Insert the place of the outlet"
        }

        require(description.trim().length > 0) {
            "Insert a description of the outlet"
        }
    }
}