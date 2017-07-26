# Xero + AWS - Show Me The Money Sample App

## How the sample app works

“Show me the Money” is a basic debt collector app.  User’s create an account, receive a verification email and login to the App.  Under settings menu, the user connects to a Xero org and once connected, can load overdue invoices into the App.

Only invoices that are overdue by 30 days with contacts that have an email address are retrieved.  On the dashboard, a list of invoices are displayed with a checkbox next to each one. Users can select one or more invoices to send an email prompting them to go to Xero’s  Online Invoice and pay the overdue invoice.

## How to Setup

Follow these steps to setup your Xero and AWS account and deploy this App.

## Sign up with Xero

### Create a Xero User Account
Create a   [30 day free Xero trial](https://www.xero.com/sg/signup/) (your user account never expires) and check out the [demo company](https://developer.xero.com/documentation/getting-started/development-accounts)you’ll use when connecting to a Xero Org.   After you sign up, check your email and activate your account.
Create a Xero Public App
Login to Xero Developer Center > [My Apps](https://app.xero.com/Application) with your new Xero User account.

Click “Add Application” button and complete required fields
* Use “Public” Type
* Application Name : Your choice
* URL for your company : You choice
* OAuth callback domain : elasticbeanstalk.com
* Checkbox to agree to T&Cs

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-1.png?raw=true)

After you click the Save button, you’ll see your Consumer Key and Secret on the right side.

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-2.png?raw=true)

## Setup Amazon Web Services
Now let’s  sign up for AWS and set up the services needed to run our Sample App.

### Create an AWS User Account
If you already have an AWS account, you can use it. However, we recommend you run our Sample App in a different account from your production workloads. A security best practice is to have separate AWS accounts for production vs sample applications, test, etc.

If you do not already have an AWS account, [sign up for one](https://portal.aws.amazon.com/gp/aws/developer/registration/index.html)

Enter your email address and choose “I am a new user” then click “Sign in using our secure server”:


![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-3.png?raw=true)

On the next page, fill in the Login Credentials. We recommend you choose a strong password:


![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-4.png?raw=true)

On the Contact Information page, fill in the details, read the AWS Customer Agreement, and click “Create Account and Continue”

On the Payment Information page, enter your credit card details and click Next. Note that our sample app may incur some small AWS service charges, but will mostly be covered by AWS Free Tier usage.

On the Identity Verification page, enter your phone number; AWS will then display a PIN on the page, call you, and prompt you to enter a PIN via your phone’s keypad.

On the Support Plan page, select **Basic** support and click **Continue**

AWS will send you an email to confirm your sign up. Click the link in the email to complete the sign-up process and log into the [AWS Console](https://us-east-1.console.aws.amazon.com/console/home?region=us-east-1).

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-5.png?raw=true)

### Setup Identity and Access Management (IAM) for user permissions
Right now, you are logged into the AWS Console as the root user. This is an all-powerful user, so we should create an alternate user with more appropriate credentials. To do this, search for IAM and click it:

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-6.png?raw=true)

On the left, click **Users** then **Add user**

Give the user a **name** and set the **Access Type** to **AWS Management Console Access** and either allow AWS to generate a password or enter a strong password yourself.


On the Set Permissions screen, choose **Attach existing policies directly** and select the **AdministratorAccess** policy:



Click Review, then Create User

**Click the link in the green box** - “Users with AWS Management Console access can sign-in at:https://XXXXXXX.signin.aws.amazon.com/console  

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-7.png?raw=true)

### Setup Cognito
You’ll need to setup a Cognito User Pool for your App. Cognito provides **user identity management**, which our example app will use.  

**Login to AWS Console** and at the top right, select the US East (N. Virginia) region:

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-8.png?raw=true)

From the Services search for Cognito.

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-9.png?raw=true)

* Click **Manage your User Pools** button
* Click *&*Create a User Pool**
* Enter a **name** for your User Pool (i.e. Show Me The Money)
* Click **Review Defaults**

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-10.png?raw=true)


Click Add app client

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-11.png?raw=true)

On the next screen click **Add an app client** … again.


Since, this is a server-side app
* **Uncheck** Generate client secret
* **Check** Enable sign-in API for server-based authentication (ADMIN_NO_SRP_AUTH)
* Click **Create app** client button.

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-12.png?raw=true)


Click **return to pool** details link and click **Create Pool** button

Under General Settings > **Message Customizations** - we recommend removing the period after the temporary password.  (it’s very easy for users to copy the period along with the temp password)

Click **Save** button

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-13.png?raw=true)

Copy the **Pool Id and** save it for use later.

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-14.png?raw=true)

Under General Settings > **App clients** - copy the **App client id** and save it for use later.


![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-15.png?raw=true)

### DynamoDB
You’ll be using AWS DynamoDB - Amazon’s managed NoSQL database - no configuration is needed to run our Sample App as it willl dynamically create a “users” and “invoices” tables.


### Create an Elastic Beanstalk Web Server
Now we’ll create a web server to host our Sample App.  Make sure you’re still in the US East (N. Virginia) region.

* From the **AWS Services**, search for **Elastic Beanstalk** 

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-16.png?raw=true)

* Click Create New Application.
* Enter a Name for your application and click Next.

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-17.png?raw=true)

* If no environments exist, click Create One Now.
* Click Create web server button.
* On Environment Type screen
	* Select Predefined configuration **Tomcat**
	* Select Environment type **Single Instance**

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-18.png?raw=true)

* For now, we’ll leave the source as Sample application and click Next.
* Set your Environment URL (this can be anything you like) and check availability and click Next.

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-19.png?raw=true)

* **Additional Resources** use defaults, click **Next**.
* **Configuration details** use defaults, click **Next**.
* **Environment Tags** use defaults, click **Next**.
* Click **Next**.
* **Permissions**, use defaults, click **Next**.
* Review, click **Launch**.

### Configure Environment Properties for Elastic Beanstalk
We need to inject some environment properties into Elastic Beanstalk for our Sample App to use. From the Elastic Beanstalk dashboard, select Configuration > Software Configuration

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-20.png?raw=true)

Scroll down to the **Environment Properties**.
**Create 7 new properties** - see the table below:


|  Property Name  | Property Value                                                                                                     |
|-----------------|--------------------------------------------------------------------------------------------------------------------|
| ConsumerKey     | Your consumer key from app.xero.com                                                                                |
| ConsumerSecret  | Your consumer secret from app.xero.com                                                                             |
| CognitoPoolId   | Your cognito pool id from earlier                                                                                  |
| CognitoClientId | Your cognito app client id                                                                                         |
| UserAgent       | Your app name                                                                                                      |
| AuthCallBackUrl | elasticbeanstalk URL + /CallbackServlet (i.e. http://showmethemoney.us-east-1.elasticbeanstalk.com/CallbackServlet)|
| FromEmail       | Your email address - which you verified in AWS SES service                                                         |

Then, click **Apply**.

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-21.png?raw=true)

### Set Permissions for Elastic Beanstalk Role
Let’s make sure we have enough permissions to run all the AWS services we are using in our Sample App.  If you kept the default permissions -  your role will be aws-elasticbeanstalk-ec2-role

* From the Services menu, select the IAM
* Select Roles and click on aws-elasticbeanstalk-ec2-role
* You’ll need to attach these policies
	* AmazonDynamoDBFullAccess
	* AmazonSESFullAccess
	* AmazonCognitoPowerUser

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-22.png?raw=true)

### Setup AWS Simple Email Service (SES)
SES is Amazon’s email sending service. SES is limited to a sandbox environment, where it can **only send to and from verified addresses** during development.  Let’s verify your email address so you can send emails from the Sample App.

* From the Services search for SES:

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-23.png?raw=true)

* Select **email addresses** at the left, then **Verify a New Email Address** and enter your email address.
* **Check your inbox** and click the verification link.

![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-24.png?raw=true)

### Deploy the Sample App
Clone Github Repo
If you haven’t already, **clone this Github repo** to your local machine.   No need to compile the code yet.  Look inside the **Target** directory for the xd-hax-17.war file.  This is what you’ll deploy in the next step. 

### Deploy WAR file to Elastic Beanstalk
Now you’re ready to deploy the sample app.

From the **Services** menu, select the **Elastic Beanstalk**.
Click on your **Application**.
Click **Upload and Deploy**
Choose the **xd-hax-17.war** file from your computer and enter a version number.


![alt text](https://github.com/XeroAPI/xdhax-java-sample/blob/master/show-me-the-money-java/image/image-25.png?raw=true)

### Testing the sample app
Once the war file has been successfully deployed, try out your sample app by pointing your browser to your Elastic Beanstalk URL.

#### Create some late invoices in Xero
* Login to Xero Demo Company and create a new Contact with an email address you  Verified for SES.
* Create a Sales Invoice with your new Contact in Xero and make it more than 30 days overdue.

#### Create a user account in the Sample App.
* Open the Sample App in your browser
* **Sign up** with your email address.
* **Go to your email Inbox and look for the verification email** with the temporary password.  Use this set a new password.
* Signin to the Sample App
Go to **Settings**, click **Connect To Xero** and Select the “Demo Company”, click OK
Once you successfully returned to the Sample App, click **Load Invoices**.

#### Chase up an overdue invoice
* Go to the Sample App > **Dashboard**.
* Check the box of an invoice with a “verified” email address.
* Click **Send Email**
* Check your inbox for email and click the link to see a Xero online invoice.

#### Modifying the sample app
Download and modify the code, compile using **mvn clean install** and deploy the new war file to Elastic Beanstalk.




For more information, tweet @xeroapi

Copyright (c) 2017 Sidney Maestre

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

