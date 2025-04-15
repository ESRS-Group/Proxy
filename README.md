Internal Proxy (Dummy)
=====

Since we cannot afford a CloudFlare subscription,
we have created a mock-up of an internal proxy routing service.

This is entirely a dummy of what a web proxy service like CloudFlare _would_ do:
it is not an integral part of our project.
We wanted a service that would divide traffic between multiple instances of our backend service,
and re-route traffic if an instance went down.

The proxy takes in a series of backend URLs, and routes traffic between available services.
If a service tests as 'down' (i.e. neither the standard HTTPS nor HTTP ports are open) then it is temporarily removed
from the services queue.

