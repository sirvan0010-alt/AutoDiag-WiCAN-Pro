// Reference-only ingestion endpoint. Not production code.
//
// Purpose: show exactly where the client → dataset boundary sits. This
// worker never holds a GitHub credential. It validates and stages records;
// a separate, offline batch job turns staged, reviewed records into a pull
// request against the dataset repository.

const MAX_RECORDS_PER_REQUEST = 25;
const FULL_VIN_LIKE = /^[A-HJ-NPR-Z0-9]{17}$/;

function isValidVehicleScope(scope) {
  if (scope === null) return true;
  if (typeof scope !== "object") return false;
  const prefix = scope.wmiVdsModelYear;
  if (typeof prefix !== "string" || prefix.length !== 10) return false;
  if (FULL_VIN_LIKE.test(prefix)) return false;
  return true;
}

function isValidPidObservation(o) {
  return typeof o === "object" && Number.isInteger(o.pid) &&
    (o.unit === null || typeof o.unit === "string") &&
    Number.isInteger(o.sampleCount) && typeof o.min === "number" &&
    typeof o.max === "number" && typeof o.mean === "number" &&
    typeof o.hadDecodeFailure === "boolean";
}

function isValidDtcObservation(o) {
  return typeof o === "object" && typeof o.code === "string" &&
    Number.isInteger(o.occurrenceCount) &&
    (o.ecuAddressHint === null || typeof o.ecuAddressHint === "string");
}

function isValidRecord(r) {
  if (typeof r !== "object" || r === null) return false;
  if (typeof r.contributionId !== "string" || r.contributionId.length === 0) return false;
  if (!Number.isInteger(r.schemaVersion) || !Number.isInteger(r.consentVersion)) return false;
  if (!isValidVehicleScope(r.vehicleScope)) return false;
  if (!/^\d{4}-\d{2}$/.test(r.monthBucket ?? "")) return false;
  if (!Array.isArray(r.pidObservations) || !r.pidObservations.every(isValidPidObservation)) return false;
  if (!Array.isArray(r.dtcObservations) || !r.dtcObservations.every(isValidDtcObservation)) return false;
  const allowedKeys = new Set(["contributionId", "schemaVersion", "consentVersion", "vehicleScope",
    "ecuSoftwareHint", "adapterFirmwareHint", "monthBucket", "pidObservations", "dtcObservations", "appVersion"]);
  return Object.keys(r).every((k) => allowedKeys.has(k));
}

export default {
  async fetch(request, env) {
    if (request.method !== "POST" || new URL(request.url).pathname !== "/v1/contributions") {
      return new Response("Not found", { status: 404 });
    }
    let body;
    try { body = await request.json(); } catch { return new Response("Invalid JSON", { status: 400 }); }
    const records = body?.records;
    if (!Array.isArray(records) || records.length === 0 || records.length > MAX_RECORDS_PER_REQUEST) {
      return new Response("Invalid batch size", { status: 400 });
    }
    for (const record of records) {
      if (!isValidRecord(record)) return new Response("Record rejected: does not match minimal contribution schema", { status: 422 });
    }
    await env.STAGING_QUEUE.send({ receivedAt: Date.now(), records });
    return new Response(null, { status: 202 });
  },
};

// Separate offline batch job: read staged records, aggregate, create a branch
// and PR in the dataset repository, require human review, then mark processed.
