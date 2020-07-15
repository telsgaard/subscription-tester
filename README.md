# subscription-tester
 
Subscription-tester is will retrieve the MTAS userprofile from the EDA node via CAI3G. After retrieving the user profile, the subscribers charging profile is extracted and written to the logger.

In order to retrieve the MTAS subscription profile, the client will perform a login sequence, where the session Id is retrieved from the login response, are used in subsequent requests. Client terminates the session by sending logout.

Username/password and IP for the EDA node is hardcoded for now.
