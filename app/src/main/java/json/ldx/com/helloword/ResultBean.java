package json.ldx.com.helloword;

import java.util.List;

/**
 * Created by Administrator on 2019/2/20 0020.
 */

public class ResultBean {

    /**
     * code : 0
     * data : {"block":[{"type":"text","line":[{"confidence":1,"word":[{"content":"lim(+-2-+3)"}]},{"confidence":1,"word":[{"content":"X-1"}]}]}]}
     * desc : success
     * sid : wcr00036136@dx2cd70fc81d506f2b00
     */

    private String code;
    private DataBean data;
    private String desc;
    private String sid;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public static class DataBean {
        private List<BlockBean> block;

        public List<BlockBean> getBlock() {
            return block;
        }

        public void setBlock(List<BlockBean> block) {
            this.block = block;
        }

        public static class BlockBean {
            /**
             * type : text
             * line : [{"confidence":1,"word":[{"content":"lim(+-2-+3)"}]},{"confidence":1,"word":[{"content":"X-1"}]}]
             */

            private String type;
            private List<LineBean> line;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public List<LineBean> getLine() {
                return line;
            }

            public void setLine(List<LineBean> line) {
                this.line = line;
            }

            public static class LineBean {
                /**
                 * confidence : 1
                 * word : [{"content":"lim(+-2-+3)"}]
                 */

                private int confidence;
                private List<WordBean> word;

                public int getConfidence() {
                    return confidence;
                }

                public void setConfidence(int confidence) {
                    this.confidence = confidence;
                }

                public List<WordBean> getWord() {
                    return word;
                }

                public void setWord(List<WordBean> word) {
                    this.word = word;
                }

                public static class WordBean {
                    /**
                     * content : lim(+-2-+3)
                     */

                    private String content;

                    public String getContent() {
                        return content;
                    }

                    public void setContent(String content) {
                        this.content = content;
                    }
                }
            }
        }
    }
}
