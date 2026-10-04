set OC_HOME=C:\Users\nitin\property-management-main\java\kafka-openshift\oc

%OC_HOME%\oc get secret postgresql -o jsonpath="{.data.database-user}" > user.txt && certutil -decode user.txt user_decoded.txt && %OC_HOME%\oc get secret postgresql -o jsonpath="{.data.database-password}" > pass.txt && certutil -decode pass.txt pass_decoded.txt && echo --- USERNAME --- && type user_decoded.txt && echo. && echo --- PASSWORD --- && type pass_decoded.txt && echo. && del user.txt user_decoded.txt pass.txt pass_decoded.txt

