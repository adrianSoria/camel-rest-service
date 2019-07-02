# camel-rest-service
example asynch REST application with spring boot and DB/JMS
This example contains a best effort 1 Phace Commit with Springs ChainedTransactionsManager.
An error will roll back the JMS message and database inserts.

REST -> JMS  -> DB -> DB -> exception!
=> rollback jms & db