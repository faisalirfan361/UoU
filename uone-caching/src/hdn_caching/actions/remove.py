import os, json
import boto3
from .base_engine import BaseEngine
from ..item_types.object_item import ObjectItem
from ..item_types.schedule_item import ScheduleItem


class RemoveEngine(BaseEngine):
    def __init__():
        super().__init__()
        self.funcname = os.getenv('REMOVE_LAMBDA', 'uOne-ingest-associations-remove')

    def remove(self, primary, sort):
        """Remove a record from the cache, based on the `key` and
        any special ID.

        Params:
        -------
        - primary: provide primary key
        - sort: provide sort key

        Returns:
        --------
        response from uOne-ingest-associations Remove request
            {
                'status': 'SUCCESS'|'FAILED'
            }
        """
        item = {
            "primary": primary,
            "sort": sort
        }

        return self._invoke_lambda(item.serialize())


    def remove_from_datastore(self, item):
        """remove a cache item from the cache

        Params:
        -------
        - item : `Item to be removed`
        """
        client = self._dynamo_client()
        table_name = os.getenv('REFERENCE_TABLE', 'References')

        table = dynamodb.Table(table_name)
    
        response = client.delete_item(
            Key={
                'object-type': item.primary,
                'object-id': item.sort
            },
        )

        print(f"Received successfull rfor delete {response} ")
        return response

    def _dynamo_client(self):
        return boto3.client('dynamodb')


    def _lambda_invoker(self):
        """Get client to invoke lambda

        Returns:
        --------
        boto3 Lambda Client
        """
        return boto3.client('lambda')


    def _invoke_lambda(self, payload: dict) -> dict:
        """Directly invoke a lambda in uOne-ingest-associations
        """
        client = self._lambda_invoker()
        # payload should be b'bytes'|file
        print(f"Invoking uOne-ingest-associations-remove lambda with: {payload}")
        res = client.invoke(FunctionName=self.funcname, InvocationType='RequestResponse', Payload=payload)

        if 200 <= res['StatusCode'] < 300:
            print(f"Received successfull response from lambda, responding with SUCCESS")
            return res['Payload']
        else:
            raise Exception(f"Invoking lambda failed with a 500-level error: {funcname}")
