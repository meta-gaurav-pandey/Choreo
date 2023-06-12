import ballerina/io;
import  ballerina/http;

public function main() returns error?{
    http:Client itune=check new("http://universities.hipolabs.com");
    json search=check itune->get("/search?country=United+States");
    io:println(search);
}
