import { useEffect, useState } from "react";
import System from "../models/system";


export default function useAppVersion() {
  const [version, setVersion] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    System.fetchAppVersion()
      .then(setVersion)
      .finally(() => setIsLoading(false));
  }, []);
  return { version, isLoading };
}
