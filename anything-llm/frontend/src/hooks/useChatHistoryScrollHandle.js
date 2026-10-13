import { useImperativeHandle } from "react";


export default function useChatHistoryScrollHandle(
  ref,
  chatHistoryRef,
  { setIsUserScrolling, isStreaming, scrollToBottom }
) {
  useImperativeHandle(
    ref,
    () => ({
      scrollToTop() {
        if (chatHistoryRef.current) {
          setIsUserScrolling(true);
          chatHistoryRef.current.scrollTo({
            top: 0,
            behavior: "smooth",
          });
        }
      },
      scrollToBottom() {
        setIsUserScrolling(true);
        scrollToBottom(isStreaming ? false : true);
      },
    }),
    [isStreaming]
  );
}
