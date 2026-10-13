import { useState, useEffect, useCallback } from "react";
const ALIGNMENT_STORAGE_KEY = "anythingllm-chat-message-alignment";


export function useChatMessageAlignment() {
  const [msgDirection, setMsgDirection] = useState(
    () => localStorage.getItem(ALIGNMENT_STORAGE_KEY) ?? "left"
  );

  useEffect(() => {
    if (msgDirection) localStorage.setItem(ALIGNMENT_STORAGE_KEY, msgDirection);
  }, [msgDirection]);

  const getMessageAlignment = useCallback(
    (role) => {
      const isLeftToRight = role === "user" && msgDirection === "left_right";
      return isLeftToRight ? "flex-row-reverse" : "";
    },
    [msgDirection]
  );

  return {
    msgDirection,
    setMsgDirection,
    getMessageAlignment,
  };
}
