import axios from 'axios';
import PubSub = require("@google-cloud/pubsub");
const axiosConfig = {
    validateStatus: (status: number) => 
        (status >= 200 && status < 300) // default
        // If a response gets delivered twice, then the second message will not correspond
        // to a receive task in Camunda, and the engine will reject the delivery with a
        // 400 "Cannot correlate message '<msg> ....':
        // We DO NOT want to retry this, it will never succeed, so mark it as a success
        // So the retry behaviour of GCF does not go into a loop
        // Note other 4xx errors like a 429, are Cloud Run or Cloud SQL being busy
        // Which we do want to retry. 400 is the MVP for at-least-once
        ||  status == 400 
}

export async function echo(event: PubSub.Message, context: any) {
    // @ts-ignore (Buffers work fine with JSON parse)
    const payload = JSON.parse(Buffer.from(event.data, 'base64'));
    console.log("Received:  " + JSON.stringify(payload));
    const url = process.env.MESSAGE_URL;
    if (url === undefined) throw new Error("Must defined MESSAGE_URL env variable");
    // Send to Camunda
    // https://docs.camunda.org/manual/7.5/reference/rest/message/post-message/#example
    const response = await axios.post(url, payload, axiosConfig); // Send message to Camunda
    console.log("Camunda response:", response.status)
};
