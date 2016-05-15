/**
 * 
 */

var myapp = angular.module("myAppReg",[]);

myapp.controller("myController", function($scope,$http) {

$scope.pushToBank=function(){
		alert("in cntrller");

		var inputData = {
				toAccountNumber:'6000025',
				toSortCode: '839999',
				paymentReference: 'SaveAndLadder',
				paymentAmount: 0.05,
				callbackUri: 'string'
				};

		alert("before post");


		$http({ method: 'POST',
        	url: 'https://bluebank.azure-api.net/api/v0.6.3/accounts/5736b3a0b5cd9bac39071438/payments',
		headers: {"Ocp-Apim-Subscription-Key": 'a563ebd1e88a4fd4837fae203d012c69',
		  "bearer": 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjdXN0b21lcklkIjoiNTczNmIzYTBiNWNkOWJhYzM5MDcxNDM3Iiwicm9sZSI6InVzZXIiLCJwcmltYXJ5U3Vic2NyaWJlcktleSI6ImE1NjNlYmQxZTg4YTRmZDQ4MzdmYWUyMDNkMDEyYzY5IiwiaWF0IjoxNDYzMjA4ODkwfQ.3wMiYgm9_k4xWhNDrAr12WcZqjudiWY-thJqZSl3j9s'
		},
		data: inputData
                                    }).then(function(response){
                                                alert("HTTP Request - Success");
                                                $scope.valid=response.data;
                                                }, function(response){
                                                            alert("HTTP Request - Error");
                                                            alert('error: '+ response.status);
                                    });
	}
});
