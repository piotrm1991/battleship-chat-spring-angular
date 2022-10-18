export class Message {
    type: string;
    sender: string;
    content: string;

    constructor(sender: string, content: string, type: string){
        this.type = type;
        this.sender = sender;
        this.content = content;
    }
  }