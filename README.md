The below repository connects an angular based UI to an AWS Cognito User pool. There are three main components. 

1) The app-header that shows the top header, including the Secure Log in link. The app-header opens the login dialog. 

2) This component displays two modal dialogs. One for login and other for forcing a change of password. The loging and change of password request are made via the Authorization service. 

3) The auth service uses the amplify api to sign in the user to the Cognito API.

4) The http interceptor. The http interceptor intercepts each http request and appends the Jwt token from the local storage as an Authentication header in the request.
