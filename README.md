# CoronaTracker
Read: https://github.com/chinmaygarg13/CoronaTracker/blob/master/CoronaTracker.pdf  

Google sheets link: https://docs.google.com/spreadsheets/d/1_651p7O1Ex8V-fTS9YmPfC9Ztn1bW0Oz4YQfZqosDiI/edit#gid=366066937 (goto Form -> Live Form for the google form). Responses by doctors/hospitals are stored here. Check the script linked with it: https://script.google.com/d/1Mw8iqb7ru8GPpDRcM0qo30X_z1FnVA6Am00P3gbwrcfi8TxbxLZ_JuPe/edit?mid=ACjPJvFyL1OigGW4sO0eY0sQT-dqFO6A1QGiqZU8QHYCHlybeXZEzouloid0O9rVKW7LL89qipVDbV3OfquG7I1eX2BOHts0H4VqEp0IeYndPpTkVOKMXn8k6i1kKNtp3Qku9Mb5vsHbfHns&uiv=2.

Firebase: https://console.firebase.google.com/u/0/project/coronap2p-c959f/database/coronap2p-c959f/data


TODO:  
1. Firebase Job Scheduler: to periodically start the job.
2. Boot Receiver: Start service after reboot.
3. Turn off battery optimization for the app.
4. Present live location of infected patients on the map (on location update of degree 1 users in the database, overwrite the entry in google sheet, using an google sheet addon display the info on a map)
5. Warn if two people come in a range of 6 feet with each other. This functionality can be disabled by the user.
