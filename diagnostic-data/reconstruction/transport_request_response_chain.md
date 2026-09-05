# SEOBD — reconstructed application-layer chain

## Strong native evidence

The native symbol graph contains a coherent chain around vehicle data:

1. `CMessageHelper::GetReqSubscribeVehicleData(bool)` — request construction helper.
2. `ReqSubscribeVehicleData` — generated request message.
3. `RespSubscribeVehicleData` — generated response message.
4. `PushVehicleDataHolder` — generated vehicle-data holder.
5. `CBLECommander::ProcessSubscripVehicleDataRequest(...)` — subscription request processor.
6. `CBLECommander::ProcessPushVehicleDataHolder(PushVehicleDataHolder const&)` — push-data processor.
7. `CBLECommander::slotDataPushReceived(QString const&, QByteArray const&)` — raw data-push entry point.

## What this proves

The application has a structured vehicle-data subscription/notification path at its native application layer. The data is not merely a collection of unrelated strings.

## What this does NOT prove yet

It does not prove:

- a specific BLE characteristic UUID;
- a specific CAN arbitration ID;
- a specific CAN payload layout;
- a specific OBD PID;
- a physical scaling factor;
- a runtime vehicle value.

## Next trace

The next static-analysis pass should follow:

`GetReqSubscribeVehicleData` → serialized request fields → transport send → subscription response → push notification framing → `PushVehicleDataHolder::_InternalParse` → semantic field accessor → UI/dashboard consumer.

This is the correct point to stop guessing and start tracing concrete serialization and call sites.
