import  ballerina/http;


service /ballerinapack on new http:Listener(8080){
    resource function get getResource() returns json|error?{
http:Client itune=check new("http://universities.hipolabs.com");
    json search=check itune->get("/search?country=United+States");
    return search;
    }
}