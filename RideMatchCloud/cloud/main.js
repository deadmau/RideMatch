function LinkedList() {
this._head = null;
this._length = 0;
}

LinkedList.prototype = {

constructor: LinkedList,

insertEnd: function(data) {

    var node = {
            data: data,
            next: null
        },
        current;

    if (this._head === null) {
        this._head = node;
    }
    else {
        current = this._head;
        while (current.next) {
            current = current.next;
        }
        current.next = node;
    }
    this._length++;
},

remove: function(node) {

    if(node === null || this._head === null) {
        return;

    } else if(node === this._head){
        this._head = node.next;
        this._length--;
        return node.data;

    } else{
        var current = this._head;

        while(current){
          if(current.next === node){
              if(node.next !== null) {
                  current.next = node.next;
              } else{
                  current.next = null;
              }
              this._length--;
              return node.data;
          }
          current = current.next;
        }
    }
},

item: function(index) {

    var current = this._head,
        i = 0;

    if (index > - 1 && index < this._length) {

        while (i++ < index) {
            current = current.next;
        }
        return current.data;

    }
    else {
        return null;
    }
},

listNearest: function(node, capacity) {

    var newList = new LinkedList();
    newList.insertEnd(this.remove(node));

    if(this._length == 0){

        return newList;
    }

    var original = new Parse.GeoPoint(node.data["location"]),
        current = this._head,

        compareNode = this._head,
        comparePoint = new Parse.GeoPoint(compareNode.data["location"]),
        distance = original.milesTo(comparePoint);

    //remove 'capacity' nodes and append it to newList
    for(var i = 1; i < capacity; i++){

        while(current.next) {

          var newNode = current.next,
              newPoint = new Parse.GeoPoint(newNode.data["location"]);

          if(original.milesTo(newPoint) < distance){
              compareNode = newNode;
              comparePoint = newPoint;
              distance = original.milesTo(newPoint);
          }

          current = current.next;

        }

        newList.insertEnd(this.remove(compareNode));
        current = this._head;

        if(current === null) {
            return newList;
        }

        compareNode = this._head;
        comparePoint = new Parse.GeoPoint(current.data["location"]);
        distance = original.milesTo(comparePoint);
    }
    return newList;
},

toArray: function() {
    var result = [],
        current = this._head;

    while (current) {
        result.push(current.data);
        current = current.next;
    }
    return result;
},

toString: function() {
    return this.toArray().toString();
},

toParseUserList: function() {

    var current = this._head,
        names = "";

    while(current) {
      names += current.data["name"] + ", Address: " + current.data["address"] + ", tel: " + current.data["phone"] + ".";
      current = current.next;
    }

    return names;
}

}

Parse.Cloud.job("matchOffer", function(request, status) {
	// Notification for Android users
	var query = new Parse.Query(Parse.Installation);
	query.containedIn('channels', ['Offer']);
 
	Parse.Push.send({
  		where: query,
  		data: {
        action: "ko.ridematch.MAIN",
    		alert: "Notification for Sunday ride"
  		}
		}, {
  		success: function() {
    		// Push was successful
    		status.success("Push completed successfully.");
  		},
  		error: function(error) {
    		// Handle error
    		status.success("Something went wrong.");
  		}
	}); 
});

Parse.Cloud.job("listMaker", function(request, status) {
  var userinfo = Parse.Object.extend('UserInfo'),
      infoQuery = new Parse.Query(userinfo);

      infoQuery.get('Zl0QIRJ0Zb', {
        success: function(object) {
          // object is an instance of Parse.Object.
          var list = object.get("userList"),
              counter = 0,
              query = new Parse.Query(Parse.User);

              query.each(function(user) {
                var username = user.get("username");
                //must store fresh user data
                 list[username] = user.get("info");
                 counter++;
              }).then(function() {
                // Set the job's success status
                object.set("userList", list);
                object.save();
                status.success(counter + " Users appended successfully.");
              }, function(error) {
                // Set the job's error status
                status.error("Uh oh, something went wrong.");
            });

          },
          error: function(object, error) {
            // error is an instance of Parse.Error.
            status.error("Uh oh, something went wrong.");
        }
    });
});

var driversList = new LinkedList(),
    attendeeList = new LinkedList();

Parse.Cloud.job("rideMatcher", function(request, status) {
  var userinfo = Parse.Object.extend('UserInfo'),
      attendance = 0,
      query = new Parse.Query(userinfo);

  query.get('Zl0QIRJ0Zb', {
        success: function(object) {
          // object is an instance of Parse.Object.
          var list = object.get("userList");

          for(var user in list) {
            if(list[user]["RideRequest"]){
              attendance++;
              if(list[user]["carOwner"]){
                //make list for drivers
                driversList.insertEnd(list[user]);
              } else{
                //make list for attendees
                attendeeList.insertEnd(list[user]);
              }
            }
          }
          // match driver to attendees
          rideMatch();
          // Set the job's success status
          status.success(attendance + " Attendance completed successfully.");
        },
        error: function(object, error) {
          // error is an instance of Parse.Error.
          status.error("Uh oh, something went wrong.");
        }
    });
});

function rideMatch() {

  var driver = driversList._head,
      driverInfo = "",
      attendeeInfo = "";

  while(driver) {

    driverInfo += driver.data["name"] + ", tel: " + driver.data["phone"] + ".";

    var capacity = driver.data["capacity"] - 1,
        driverPoint = new Parse.GeoPoint(driver.data["location"]);

    if(attendeeList._length > 0){

      var attendee = attendeeList._head,
          nearest = attendeeList._head,
          attendPoint = new Parse.GeoPoint(nearest.data["location"]),
          distance = driverPoint.milesTo(attendPoint);

      while(attendee.next){

        var nextAttend = attendee.next,
            newPoint = new Parse.GeoPoint(nextAttend.data["location"]);

        if(driverPoint.milesTo(newPoint) < distance){
          nearest = nextAttend;
          attendPoint = newPoint;
          distance = driverPoint.milesTo(newPoint);
        }
        attendee = nextAttend;
      }

      var finalList = attendeeList.listNearest(nearest, capacity);

      attendeeInfo += finalList.toParseUserList() + "/";

    } else{

      attendeeInfo += "none./";
    }
      driver = driver.next;
    }
    sendResult(driverInfo, attendeeInfo);
}

function sendResult(driverinfo, attendeeinfo) {
  //send the resulting group
  var query = new Parse.Query(Parse.Installation);
  query.containedIn('channels', ["Result"]);

    Parse.Push.send({
        where: query,
        data: {
          action: "ko.ridematch.UPDATE_STATUS",
          driver: driverinfo,
          attendee: attendeeinfo,
          alert: "Match Results"
        }
      }, {
        success: function() {
          // Push was successful
          status.success("Push completed successfully.");
        },
        error: function(error) {
          // Handle error
          status.success("Something went wrong.");
        }
    });
}
