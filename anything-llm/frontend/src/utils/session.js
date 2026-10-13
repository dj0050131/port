import { API_BASE } from "./constants";
import { baseHeaders } from "./request";

export default async function validateSessionTokenForUser() {
  const isValidSession = await fetch(`${API_BASE}/system/check-token`, {
    method: "GET",
    cache: "default",
    headers: baseHeaders(),
  })
    .then((res) => res.status === 200)
    .catch(() => false);

  return isValidSession;
}
